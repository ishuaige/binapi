package com.niuma.binapi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.niuma.binapi.model.dto.user.UserRegisterRequest;
import com.niuma.binapi.model.vo.UserDevKeyVO;
import com.niuma.binapicommon.model.entity.User;
import com.niuma.binapicommon.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户服务
 *
 * @author niuma
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterRequest
     * @param request
     * @return 新用户id
     */
    Long userRegister(UserRegisterRequest userRegisterRequest,HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request,HttpServletResponse response);

    /**
     * 使用手机号登录
     * @param phoneNum
     * @param phoneCaptcha
     * @param request
     * @return
     */
    User userLoginBySms(String phoneNum, String phoneCaptcha, HttpServletRequest request,HttpServletResponse response);

    /**
     * 获取一个脱敏的用户信息
     *
     * @param originUser
     * @return
     */
    User initLoginUser(User originUser,HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 通过cookie获取用户
     * @param cookie
     * @return
     */
    UserVO getLoginUser(String cookie);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 生成图像验证码
     * @param request
     * @param response
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);

    /**
     * 发送短信验证码
     * @param phoneNum
     * @return
     */
    Boolean sendSmsCaptcha(String phoneNum);

    /**
     * 重新生成ak，sk
     * @param request
     * @return
     */
    UserDevKeyVO genkey(HttpServletRequest request);
}
