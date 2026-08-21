package com.ruoyi.common.exception.user;

/**
 * 用户错误最大次数异常类
 * <p>
 * 【触发时机】密码连续输错达到上限（默认5次，sys_config的sys.user.password.retry.limit配置）后锁定账号，
 * 由SysPasswordService抛出。
 * 【参数填充】构造传入retryLimitCount和lockTime两个参数，填充messages.properties文案占位符：
 * "密码输入错误{0}次，账号锁定{1}分钟" → 前端看到"密码输入错误5次，账号锁定10分钟"。
 * 
 * @author ruoyi
 */
public class UserPasswordRetryLimitExceedException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserPasswordRetryLimitExceedException(int retryLimitCount, int lockTime)
    {
        super("user.password.retry.limit.exceed", new Object[] { retryLimitCount, lockTime });
    }
}
