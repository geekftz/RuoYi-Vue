package com.ruoyi.common.exception.user;

import com.ruoyi.common.exception.base.BaseException;

/**
 * 用户信息异常类
 * <p>
 * 【定位】用户域（登录/注册/密码）异常树的父类，固定module="user"。
 * 【子类】CaptchaException（验证码错误）、UserPasswordNotMatchException（密码错误）、
 * UserPasswordRetryLimitExceedException（密码重试超限锁定）等，全部在SysLoginService登录流程中抛出。
 * 【i18n】构造传入的code是messages.properties的key，见BaseException注释。
 * 
 * @author ruoyi
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}
