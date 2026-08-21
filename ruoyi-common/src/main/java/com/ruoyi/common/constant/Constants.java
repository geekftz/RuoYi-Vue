package com.ruoyi.common.constant;

import java.util.Locale;
import io.jsonwebtoken.Claims;

/**
 * 通用常量信息
 * <p>
 * 【架构位置】common模块 → constant，全系统通用常量总纲。按用途分几组：
 * ①编码/协议（UTF8、HTTP）；②登录日志标识（LOGIN_SUCCESS等，写入sys_logininfor.status/message）；
 * ③权限标识（ALL_PERMISSION超管通配符）；④JWT载荷key名（LOGIN_USER_KEY等，TokenService签发/解析用）；
 * ⑤安全白名单/黑名单（JSON反序列化白名单、定时任务包名限制）。
 * 
 * @author ruoyi
 */
public class Constants
{
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * 系统语言
     */
    public static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    /**
     * www主域
     */
    public static final String WWW = "www.";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 所有权限标识（超管通配符，PermissionService匹配时见到它直接放行一切权限校验）
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 管理员角色权限标识
     */
    public static final String SUPER_ADMIN = "admin";

    /**
     * 角色权限分隔符
     */
    public static final String ROLE_DELIMITER = ",";

    /**
     * 权限标识分隔符
     */
    public static final String PERMISSION_DELIMITER = ",";

    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌
     */
    public static final String TOKEN = "token";

    /**
     * 令牌前缀（Authorization请求头的标准格式：Authorization: Bearer xxxxx，解析时先去掉此前缀再校验JWT）
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀（JWT载荷中存放登录用户UUID的key名：JWT解开claims.get("login_user_key")拿到Redis key后缀）
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 用户ID
     */
    public static final String JWT_USERID = "userid";

    /**
     * 用户名称
     */
    public static final String JWT_USERNAME = Claims.SUBJECT;

    /**
     * 用户头像
     */
    public static final String JWT_AVATAR = "avatar";

    /**
     * 创建时间
     */
    public static final String JWT_CREATED = "created";

    /**
     * 用户权限
     */
    public static final String JWT_AUTHORITIES = "authorities";

    /**
     * 资源映射路径 前缀（本地上传文件的访问URL前缀：application.yml的ruoyi.profile配置文件存放路径，
     *  如D:/ruoyi/uploadPath下的文件通过 http://域名/profile/xxx.jpg 访问，ResourcesConfig做了路径映射）
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 远程方法调用
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 远程方法调用
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 远程方法调用
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     * <p>
     * 【安全背景】fastjson的AutoType反序列化曾爆出多个RCE漏洞（攻击者构造恶意JSON实例化危险类）。
     * 若依用fastjson2序列化Redis，这里限定只允许反序列化com.ruoyi包下的类，从根上堵住AutoType攻击。
     */
    public static final String[] JSON_WHITELIST_STR = { "com.ruoyi" };

    /**
     * 定时任务白名单配置（仅允许访问的包名，如其他需要可以自行添加）
     * <p>
     * 【安全背景】若依定时任务支持页面配置Bean调用字符串（如ryTask.ryParams('ry')），
     * 不限制的话可被用来调用任意Spring Bean执行危险操作，故只允许调用com.ruoyi.quartz.task包下的任务类。
     */
    public static final String[] JOB_WHITELIST_STR = { "com.ruoyi.quartz.task" };

    /**
     * 定时任务违规的字符
     */
    public static final String[] JOB_ERROR_STR = { "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", "com.ruoyi.common.utils.file", "com.ruoyi.common.config", "com.ruoyi.generator" };

    /**
     * 部门相关常量（数据范围五种取值，与sys_role.data_scope字段对应，DataScopeAspect据此拼过滤SQL）
     */
    public static class Dept
    {
        /**
         * 全部数据权限
         */
        public static final String DATA_SCOPE_ALL = "1";

        /**
         * 自定数据权限
         */
        public static final String DATA_SCOPE_CUSTOM = "2";

        /**
         * 部门数据权限
         */
        public static final String DATA_SCOPE_DEPT = "3";

        /**
         * 部门及以下数据权限
         */
        public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

        /**
         * 仅本人数据权限
         */
        public static final String DATA_SCOPE_SELF = "5";
    }
}
