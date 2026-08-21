package com.ruoyi.common.exception.user;

/**
 * 黑名单IP异常类
 * <p>
 * 【触发时机】登录用户的IP在系统黑名单中（sys_config的sys.login.blackIPList配置），
 * SysLoginService登录前置校验时抛出，提示"当前IP已被列入黑名单"。
 * 【i18n key】"login.blocked"对应messages.properties中的黑名单提示文案。
 * 
 * @author ruoyi
 */
public class BlackListException extends UserException
{
    private static final long serialVersionUID = 1L;

    public BlackListException()
    {
        // 无需参数，直接传入i18n消息key
        super("login.blocked", null);
    }
}
