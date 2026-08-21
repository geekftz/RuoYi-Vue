package com.ruoyi.framework.security.context;

import org.springframework.security.core.Authentication;

/**
 * 身份验证信息
 * <p>
 * 【架构位置】ruoyi-framework / security / context层 —— 登录认证过程中的临时上下文。
 * 【作用】用 ThreadLocal 暂存当前线程正在认证的 Authentication 对象（含用户输入的用户名/密码）。
 * 典型场景：登录认证前 SysLoginService 把凭证存进来，UserDetailsServiceImpl 查库时取出用户名使用。
 * <p>
 * 【为什么用ThreadLocal】一次HTTP请求由一个线程处理，ThreadLocal保证同一线程内随时可取、
 * 不同请求之间互不干扰；请求结束必须 clearContext() 防止线程池复用导致的数据串号。
 *
 * @author ruoyi
 */
public class AuthenticationContextHolder
{
    /** 线程本地变量：每个请求线程各持有一份独立的Authentication，天然线程安全 */
    private static final ThreadLocal<Authentication> contextHolder = new ThreadLocal<>();

    /** 获取当前线程的认证信息 */
    public static Authentication getContext()
    {
        return contextHolder.get();
    }

    /** 向当前线程写入认证信息（登录校验前调用） */
    public static void setContext(Authentication context)
    {
        contextHolder.set(context);
    }

    /** 清除当前线程的认证信息（用完必须清理，防线程复用串数据） */
    public static void clearContext()
    {
        contextHolder.remove();
    }
}
