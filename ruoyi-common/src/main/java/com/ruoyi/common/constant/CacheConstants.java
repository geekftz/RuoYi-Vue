package com.ruoyi.common.constant;

/**
 * 缓存的key 常量
 * <p>
 * 【架构位置】common模块 → constant，Redis key前缀统一定义处。
 * 【设计意图】集中管理避免key散落各处拼错，也便于在Redis客户端/监控中按前缀归类查看。
 * 实际key = 前缀 + 业务标识，如 login_tokens:abc123-uuid。
 * 
 * @author ruoyi
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key（TokenService写入：login_tokens:{uuid} → LoginUser，默认720分钟过期，即12小时）
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key（CaptchaController写入：captcha_codes:{uuid} → 算式答案，2分钟过期）
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key（SysConfigServiceImpl写入：sys_config:{配置键名} → 配置值）
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key（SysDictTypeServiceImpl写入：sys_dict:{字典类型} → List<SysDictData>）
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key（SysPasswordService写入：pwd_err_cnt:{用户名} → 错误次数，默认锁定阈值5次）
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
}
