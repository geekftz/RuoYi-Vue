package com.ruoyi.common.exception.user;

/**
 * 用户密码不正确或不符合规范异常类
 * <p>
 * 【触发时机】登录时BCrypt密码比对失败（SysPasswordService.validate抛出），
 * 或修改密码时新密码不符合复杂度要求。
 * 【密码重试联动】每次抛出本异常前，SysPasswordService会在Redis中累计失败次数，
 * 达到上限后改抛UserPasswordRetryLimitExceedException锁定账号。
 * 
 * @author ruoyi
 */
public class UserPasswordNotMatchException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserPasswordNotMatchException()
    {
        // i18n key，对应"用户不存在/密码错误"文案
        super("user.password.not.match", null);
    }
}
