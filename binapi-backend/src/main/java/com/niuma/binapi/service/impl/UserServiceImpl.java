package com.niuma.binapi.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.niuma.binapi.common.RedisTokenBucket;
import com.niuma.binapi.model.vo.UserDevKeyVO;
import com.niuma.binapicommon.constant.UserConstant;
import com.niuma.binapi.mapper.UserMapper;
import com.niuma.binapi.model.dto.user.UserRegisterRequest;
import com.niuma.binapi.service.UserService;
import com.niuma.binapi.utils.SmsUtils;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.dto.SmsDTO;
import com.niuma.binapicommon.model.entity.User;
import com.niuma.binapicommon.model.vo.UserVO;
import com.niuma.binapicommon.utils.AuthPhoneNumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.niuma.binapicommon.constant.CookieConstant.COOKIE_KEY;
import static com.niuma.binapicommon.constant.CookieConstant.COOKIE_USER_KEY;
import static com.niuma.binapicommon.constant.UserConstant.*;


/**
 * 用户服务实现类
 *
 * @author niuma
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String,String> redisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisTokenBucket redisTokenBucket;
    @Resource
    private SmsUtils smsUtils;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "dogbin";

    /**
     * 图片验证码 redis 前缀
     */
    private static final String CAPTCHA_PREFIX = "api:captchaId:";

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        String userName = userRegisterRequest.getUserName();
        String userPassword = userRegisterRequest.getUserPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String captcha = userRegisterRequest.getCaptcha();
        String phoneNum = userRegisterRequest.getPhoneNum();
        String phoneCaptcha = userRegisterRequest.getPhoneCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, captcha,phoneNum,phoneCaptcha,userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userName.length()>USERNAME_LENGTH){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户昵称应该小于7个字！");
        }
        if (userAccount.length() < USERACCOUNT_MINLENGTH || userAccount.length() > USERACCOUNT_MAXLENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短或过长");
        }
        if (userPassword.length() < USERPASSWORD_LENGTH || checkPassword.length() < USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        AuthPhoneNumberUtil authPhoneNumberUtil = new AuthPhoneNumberUtil();
        if (!authPhoneNumberUtil.isPhoneNum(phoneNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号错误");
        }
        //图形验证码
        String signature = request.getHeader("signature");
        if (StringUtils.isEmpty(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String checkCaptcha =  redisTemplate.opsForValue().get(CAPTCHA_PREFIX + signature);
        if (StringUtils.isEmpty(checkCaptcha) || !captcha.equals(checkCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片验证码过期或错误！");
        }

        if(!smsUtils.verifyCode(phoneNum, phoneCaptcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机验证码过期或错误！");
        }

        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            //3.分配ak，sk
            UserDevKeyVO userDevKeyVO = genKey(userAccount);
            String accessKey = userDevKeyVO.getAccessKey();
            String secretKey = userDevKeyVO.getSecretKey();
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(userName);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setPhoneNum(phoneNum);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request,HttpServletResponse response) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        user = initLoginUser(user, response);
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        return user;
    }

    @Override
    public User userLoginBySms(String phoneNum, String phoneCaptcha, HttpServletRequest request,HttpServletResponse response) {
        boolean verifyCode = smsUtils.verifyCode(phoneNum, phoneCaptcha);
        if(!verifyCode){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机验证码错误！");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phoneNum",phoneNum);
        User user = this.getOne(queryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在！");
        }
        User safetyUser = this.initLoginUser(user,response);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public User initLoginUser(User originUser,HttpServletResponse response) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setUserAvatar(originUser.getUserAvatar());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setAccessKey(originUser.getAccessKey());
        safetyUser.setSecretKey(originUser.getSecretKey());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        // 生成JWT
        String sign = JWT.create()
                .addPayloads(new HashMap() {{
                    put(COOKIE_USER_KEY, safetyUser);
                }})
                .setSigner(JWTSignerUtil.hs256("niuma".getBytes()))
                .setExpiresAt(DateUtil.offsetDay(new Date(), 30)).sign();
        Cookie cookie = new Cookie(COOKIE_KEY, sign);
        cookie.setPath("/api");
        response.addCookie(cookie);
        return safetyUser;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public UserVO getLoginUser(String cookie) {
        JWT jwt = JWTUtil.parseToken(cookie);
        try {
            JWTValidator.of(jwt).validateAlgorithm(JWTSignerUtil.hs256("niuma".getBytes())).validateDate();
        } catch (ValidateException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,e.getMessage());
        }
        JWTPayload payload = jwt.getPayload();
        JSONObject userVOObj = (JSONObject) payload.getClaim(COOKIE_USER_KEY);
        UserVO userVO = JSONUtil.toBean(userVOObj, UserVO.class);
        return userVO;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        //前端必须传一个 signature 来作为唯一标识
        String signature = request.getHeader("signature");
        if (StringUtils.isEmpty(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        try {
            // 自定义纯数字的验证码（随机4位数字，可重复）
            RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
            lineCaptcha.setGenerator(randomGenerator);
            //设置响应头
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "No-cache");
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature, lineCaptcha.getCode());
            // 将验证码设置到Redis中,2分钟过期
            redisTemplate.opsForValue().set(CAPTCHA_PREFIX + signature, lineCaptcha.getCode(), 2, TimeUnit.MINUTES);
            // 关闭流
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean sendSmsCaptcha(String phoneNum) {

        if (StringUtils.isEmpty(phoneNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
        }
        AuthPhoneNumberUtil authPhoneNumberUtil = new AuthPhoneNumberUtil();

        // 手机号码格式校验
        boolean checkPhoneNum = authPhoneNumberUtil.isPhoneNum(phoneNum);
        if (!checkPhoneNum) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }

        //生成随机验证码
        int code = (int) ((Math.random() * 9 + 1) * 10000);
        SmsDTO smsDTO = new SmsDTO(phoneNum,String.valueOf(code));

        return smsUtils.sendSms(smsDTO);
    }

    @Override
    public UserDevKeyVO genkey(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        UserDevKeyVO userDevKeyVO = genKey(loginUser.getUserAccount());
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userAccount",loginUser.getUserAccount());
        updateWrapper.eq("id",loginUser.getId());
        updateWrapper.set("accessKey",userDevKeyVO.getAccessKey());
        updateWrapper.set("secretKey",userDevKeyVO.getSecretKey());
        this.update(updateWrapper);
        loginUser.setAccessKey(userDevKeyVO.getAccessKey());
        loginUser.setSecretKey(userDevKeyVO.getSecretKey());
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, loginUser);
        return userDevKeyVO;
    }

    private UserDevKeyVO genKey(String userAccount){
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
        UserDevKeyVO userDevKeyVO = new UserDevKeyVO();
        userDevKeyVO.setAccessKey(accessKey);
        userDevKeyVO.setSecretKey(secretKey);
        return userDevKeyVO;
    }

}




