package com.ruoyi.common.enums;

/**
 * 限流类型
 * <p>
 * 【使用者】@RateLimiter注解的limitType属性，RateLimiterAspect据此决定限流计数key的拼法：
 * DEFAULT：按方法签名计数（所有用户共享配额）；IP：按方法签名+请求者IP计数（每个IP独立配额，防刷接口推荐）。
 *
 * @author ruoyi
 */

public enum LimitType
{
    /**
     * 默认策略全局限流
     */
    DEFAULT,

    /**
     * 根据请求者IP进行限流
     */
    IP
}
