package com.ruoyi.common.exception.user;

/**
 * 验证码错误异常类
 * <p>
 * 【触发时机】登录/注册时用户输入的验证码答案与Redis中正确答案不一致（SysLoginService.validateCaptcha抛出）。
 * 【i18n key】"user.jcaptcha.error"对应messages.properties中的"验证码错误"文案。
 * 【前端表现】前端收到错误后自动刷新验证码图片，提示用户重新输入。
 * 
 * @author ruoyi
 */
public class CaptchaException extends UserException
{
    private static final long serialVersionUID = 1L;

    public CaptchaException()
    {
        super("user.jcaptcha.error", null);
    }
}
