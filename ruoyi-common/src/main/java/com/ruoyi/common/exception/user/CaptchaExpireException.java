package com.ruoyi.common.exception.user;

/**
 * 验证码失效异常类
 * <p>
 * 【触发时机】登录时验证码已过期（Redis中验证码key默认2分钟有效期，超时自动删除），
 * SysLoginService.validateCaptcha从Redis取不到验证码时抛出。
 * 【与CaptchaException区别】本异常是"验证码不存在/已过期"，CaptchaException是"验证码答案输错"。
 * 【前端表现】前端收到"验证码已失效"后自动刷新验证码图片，用户重新输入即可。
 * 
 * @author ruoyi
 */
public class CaptchaExpireException extends UserException
{
    private static final long serialVersionUID = 1L;

    public CaptchaExpireException()
    {
        // i18n key，对应"验证码已失效"文案
        super("user.jcaptcha.expire", null);
    }
}
