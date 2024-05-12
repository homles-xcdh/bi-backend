package com.demo.springbootinit.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 盐值，混淆密码
     */
    String SALT = "bi-backend";

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    /**
     * 默认头像
     */
    String DEFAULT_USER_AVATAR = "https://imgs.design006.com/Upload/test/Design006_20200905085032928.png";

    /**
     * 默认用户名
     */
    String DEFAULT_USERNAME = "search";
}
