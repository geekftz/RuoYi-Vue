package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.enums.LimitType;

/**
 * 限流注解
 * <p>
 * 【作用】标在Controller方法上，限制接口在单位时间内的调用次数，超限直接报错"访问过于频繁"。
 * 【处理链路】RateLimiterAspect切面拦截本注解 → 用Redis+Lua脚本做原子计数（key=rate_limit:方法签名[:IP]）→
 *             次数超限抛ServiceException。
 * 【限流类型】LimitType.DEFAULT按用户限流（同一用户限次）；LimitType.IP按IP限流（同一IP限次，防刷接口常用）。
 * 【使用示例】@RateLimiter(time = 60, count = 10, limitType = LimitType.IP) —— 同一IP每分钟最多调10次。
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter
{
    /**
     * 限流key
     */
    public String key() default CacheConstants.RATE_LIMIT_KEY;

    /**
     * 限流时间,单位秒
     */
    public int time() default 60;

    /**
     * 限流次数
     */
    public int count() default 100;

    /**
     * 限流类型
     */
    public LimitType limitType() default LimitType.DEFAULT;
}
