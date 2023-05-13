package com.niuma.binapicommon.constant;

/**
 * 用户常量
 *
 * @author niuma
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 系统用户 id（虚拟用户）
     */
    long SYSTEM_USER_ID = 0;

    //  region 权限

    /**
     * 默认权限
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员权限
     */
    String ADMIN_ROLE = "admin";

    // endregion

    int USERACCOUNT_MINLENGTH = 4;
    int USERACCOUNT_MAXLENGTH = 13;
    int USERNAME_LENGTH = 7;

    int USERPASSWORD_LENGTH = 8;
}
