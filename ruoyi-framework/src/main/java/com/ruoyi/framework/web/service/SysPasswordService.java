package com.ruoyi.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.exception.user.UserPasswordRetryLimitExceedException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;

/**
 * 登录密码校验服务
 * <p>
 * 核心职责：校验用户登录密码是否正确，并实现密码错误次数限制与账户锁定机制。
 * </p>
 * <p>
 * 密码校验流程：
 * 1. 从ThreadLocal中获取当前请求的用户名和密码
 * 2. 从Redis中获取该用户的密码错误次数
 * 3. 若错误次数超过上限，抛出UserPasswordRetryLimitExceedException（账户锁定）
 * 4. 校验密码是否匹配，不匹配则错误次数+1并缓存到Redis（设置锁定过期时间）
 * 5. 密码匹配则清除错误次数缓存
 * </p>
 *
 * @author ruoyi
 */
@Component
public class SysPasswordService
{
    /** Redis 缓存 */
    @Autowired
    private RedisCache redisCache;

    /** 密码最大重试次数 */
    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    /** 账户锁定时间（分钟） */
    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    /**
     * 获取登录账户密码错误次数的Redis缓存键名
     * <p>
     * key格式：pwd_err_cnt:{username}，用于记录该用户的密码错误次数
     * </p>
     *
     * @param username 用户名
     * @return Redis缓存键名
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    /**
     * 校验用户密码（Spring Security认证流程中调用）
     * <p>
     * 业务流程：
     * 1. 从ThreadLocal上下文中获取用户输入的用户名和密码
     * 2. 从Redis获取该用户已累计的密码错误次数
     * 3. 若错误次数达到上限（默认5次），抛出账户锁定异常
     * 4. 校验密码是否匹配：
     *    - 不匹配：错误次数+1，写入Redis（设置锁定过期时间），抛出密码不匹配异常
     *    - 匹配：清除错误次数缓存
     * </p>
     *
     * @param user 数据库中查询到的用户信息（含加密后的密码）
     */
    public void validate(SysUser user)
    {
        // 从ThreadLocal中获取Spring Security认证令牌（在SysLoginService.login中设置）
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        // 从Redis中获取该用户已有的密码错误次数
        Integer retryCount = redisCache.getCacheObject(getCacheKey(username));

        if (retryCount == null)
        {
            retryCount = 0;
        }

        // 密码错误次数达到上限，账户被锁定
        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }

        if (!matches(user, password))
        {
            // 密码不匹配：错误次数+1并缓存到Redis（按锁定时间过期，过期后自动解锁）
            retryCount = retryCount + 1;
            redisCache.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
            throw new UserPasswordNotMatchException();
        }
        else
        {
            // 密码匹配：清除之前的错误次数缓存
            clearLoginRecordCache(username);
        }
    }

    /**
     * 判断密码是否匹配
     *
     * @param user 用户信息
     * @param rawPassword 原始密码
     * @return 是否匹配
     */
    public boolean matches(SysUser user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    /**
     * 清除登录错误记录缓存
     *
     * @param loginName 登录名
     */
    public void clearLoginRecordCache(String loginName)
    {
        if (redisCache.hasKey(getCacheKey(loginName)))
        {
            redisCache.deleteObject(getCacheKey(loginName));
        }
    }
}
