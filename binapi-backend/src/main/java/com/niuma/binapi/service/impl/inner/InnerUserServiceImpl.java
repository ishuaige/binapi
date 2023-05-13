package com.niuma.binapi.service.impl.inner;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.niuma.binapi.mapper.UserMapper;
import com.niuma.binapi.service.UserService;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.entity.User;
import com.niuma.binapicommon.model.vo.UserVO;
import com.niuma.binapicommon.service.InnerUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

import static com.niuma.binapicommon.constant.CookieConstant.COOKIE_KEY;
import static com.niuma.binapicommon.constant.CookieConstant.COOKIE_USER_KEY;


/**
 * @author niuma
 * @create 2023-02-27 16:13
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    UserMapper userMapper;
    @Resource
    UserService userService;

    @Override
    public User getInvokeUser(String ak) {
        if (StringUtils.isAnyBlank(ak)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey", ak);
        User user = userMapper.selectOne(queryWrapper);

        return user;
    }

    @Override
    public UserVO getLoginUser(String cookie) {
        return userService.getLoginUser(cookie);
    }
}
