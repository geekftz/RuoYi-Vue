package com.ruoyi.common.core.domain.model;

/**
 * 用户登录对象
 * <p>
 * 【架构位置】common模块 → core/domain/model，登录接口的入参DTO（Data Transfer Object）。
 * 【调用链路】前端登录页提交表单 → POST /login（JSON格式）→ Spring自动把JSON反序列化为本对象 →
 *             SysLoginController.login(@RequestBody LoginBody)接收 → 传给SysLoginService.login()处理。
 * 【字段与前端对应】username/password对应账号密码输入框；code对应用户输入的验证码答案；
 *             uuid对应获取验证码图片时后端返回的uuid（验证码答案存在Redis的key是"captcha_codes:"+uuid）。
 * 【注解专项】@RequestBody：告诉Spring从HTTP请求体（Body）中读取JSON并转成Java对象，
 *             前端axios发的是application/json格式数据时用它接收。
 * 
 * @author ruoyi
 */
public class LoginBody
{
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 验证码
     * <p>
     * 【验证码链路】①前端先调GET /captchaImage获取验证码图片+uuid →
     * ②后端把正确答案存Redis（key=captcha_codes:uuid，2分钟过期）→
     * ③用户看图输入答案提交登录 → ④后端用uuid从Redis取正确答案与本code比对。
     * 验证码开关由sys_config表的sys.account.captchaEnabled配置控制，关闭时此字段可不传。
     */
    private String code;

    /**
     * 唯一标识
     * <p>
     * 【用途】与code配套使用：作为Redis中验证码答案的key后缀，标识"这个验证码答案是给哪次登录页面加载的"。
     * 每次刷新验证码图片都会生成新uuid，防止答案被复用爆破。
     */
    private String uuid;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}
