package com.ruoyi.common.exception.user;

/**
 * 用户不存在异常类
 * <p>
 * 【触发时机】登录时根据用户名查不到用户记录（SysLoginService登录流程中用户查询为空时抛出）。
 * 【安全提示】与密码错误异常（UserPasswordNotMatchException）区分开，
 * 但实际生产环境常统一提示"用户名或密码错误"防止恶意探测有效用户名，若依此处分开提示便于学习理解。
 * 
 * @author ruoyi
 */
public class UserNotExistsException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserNotExistsException()
    {
        // i18n key，对应"用户不存在/密码错误"文案
        super("user.not.exists", null);
    }
}
