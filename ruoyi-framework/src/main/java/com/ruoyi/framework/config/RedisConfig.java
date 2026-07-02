package com.ruoyi.framework.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * <p>
 * 配置 RedisTemplate 的序列化策略，并注册限流用的 Lua 脚本。
 * </p>
 * <p>
 * 序列化策略：
 * - Key：使用 StringRedisSerializer，保证 key 在 Redis 中以可读字符串存储
 * - Value：使用 FastJson2JsonRedisSerializer，将 Java 对象序列化为 JSON 字符串存储
 * - Hash Key/Value：同上
 * </p>
 *
 * @author ruoyi
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport
{
    /**
     * 配置 RedisTemplate，使用 StringRedisSerializer 序列化 key，FastJson2JsonRedisSerializer 序列化 value
     */
    @Bean
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 限流 Lua 脚本配置
     */
    @Bean
    public DefaultRedisScript<Long> limitScript()
    {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * 限流 Lua 脚本内容
     * <p>
     * 脚本逻辑：
     * 1. 从 KEYS[1] 获取当前计数，如果已超过阈值则直接返回当前值（拒绝请求）
     * 2. 否则 INCR 自增计数，如果是第一次（值为1）则设置过期时间
     * 3. 返回当前计数值，由调用方判断是否超限
     * </p>
     * <p>
     * 使用 Lua 脚本保证「读取 + 自增 + 设置过期」三步操作的原子性，
     * 避免并发请求导致的限流不准确问题。
     * </p>
     *
     * @return Lua 脚本字符串
     */
    private String limitScriptText()
    {
        return "local key = KEYS[1]\n" +
                "local count = tonumber(ARGV[1])\n" +
                "local time = tonumber(ARGV[2])\n" +
                "local current = redis.call('get', key);\n" +
                "if current and tonumber(current) > count then\n" +
                "    return tonumber(current);\n" +
                "end\n" +
                "current = redis.call('incr', key)\n" +
                "if tonumber(current) == 1 then\n" +
                "    redis.call('expire', key, time)\n" +
                "end\n" +
                "return tonumber(current);";
    }
}
