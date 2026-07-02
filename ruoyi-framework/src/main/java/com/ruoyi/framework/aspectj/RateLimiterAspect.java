package com.ruoyi.framework.aspectj;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;

/**
 * 接口限流切面
 * <p>
 * 通过 AOP 拦截带有 @RateLimiter 注解的接口，基于 Redis + Lua 脚本实现滑动窗口限流。
 * 在指定时间窗口内，限制接口的访问次数，超过阈值则拒绝请求。
 * </p>
 * <p>
 * 限流维度（LimitType）：
 * - DEFAULT：默认限流，基于方法名+类名生成key，所有用户共享限流配额
 * - IP：基于客户端IP生成key，每个IP独立限流
 * </p>
 *
 * @author ruoyi
 */
@Aspect
@Component
public class RateLimiterAspect
{
    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    /** Redis 操作模板 */
    private RedisTemplate<Object, Object> redisTemplate;

    /** 限流 Lua 脚本 */
    private RedisScript<Long> limitScript;

    @Autowired
    public void setRedisTemplate1(RedisTemplate<Object, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setLimitScript(RedisScript<Long> limitScript)
    {
        this.limitScript = limitScript;
    }

    /**
     * 前置通知：执行限流校验
     * <p>
     * 业务流程：
     * 1. 从注解获取限流时间窗口和最大请求次数
     * 2. 生成限流缓存的组合key（包含类名、方法名，可选IP）
     * 3. 通过 Redis Lua 脚本原子性地执行：自增计数 + 判断是否超限 + 设置过期时间
     * 4. 如果当前计数超过阈值，抛出 ServiceException 拒绝请求
     * </p>
     *
     * @param point       切点
     * @param rateLimiter 限流注解
     * @throws Throwable 限流异常
     */
    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable
    {
        // 获取限流时间窗口（秒）
        int time = rateLimiter.time();
        // 获取时间窗口内允许的最大请求次数
        int count = rateLimiter.count();

        // 生成限流缓存的组合key
        String combineKey = getCombineKey(rateLimiter, point);
        List<Object> keys = Collections.singletonList(combineKey);
        try
        {
            // 执行Lua脚本：原子性地自增计数并检查是否超限、设置过期时间
            Long number = redisTemplate.execute(limitScript, keys, count, time);
            if (StringUtils.isNull(number) || number.intValue() > count)
            {
                // 当前请求次数超过阈值，拒绝访问
                throw new ServiceException("访问过于频繁，请稍候再试");
            }
            log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), combineKey);
        }
        catch (ServiceException e)
        {
            // 限流异常直接抛出
            throw e;
        }
        catch (Exception e)
        {
            // Redis异常等非业务异常，返回服务器限流异常
            throw new RuntimeException("服务器限流异常，请稍候再试");
        }
    }

    /**
     * 生成限流缓存的组合key
     * <p>
     * key组成规则：
     * - 自定义key前缀（rateLimiter.key()）
     * - 如果是IP限流，追加客户端IP
     * - 类全限定名 + 方法名
     * </p>
     * <p>
     * 例如：rate_limit-192.168.1.1-com.ruoyi.web.controller.system.SysUserController-list
     * </p>
     *
     * @param rateLimiter 限流注解
     * @param point       切点
     * @return 组合后的限流key
     */
    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point)
    {
        StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
        // IP限流模式下，将客户端IP加入key，实现每个IP独立限流
        if (rateLimiter.limitType() == LimitType.IP)
        {
            stringBuffer.append(IpUtils.getIpAddr()).append("-");
        }
        // 获取目标方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        // 追加类名和方法名，确保不同接口的限流key不冲突
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }
}
