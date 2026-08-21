package com.ruoyi.framework.config;

import java.nio.charset.Charset;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.ruoyi.common.constant.Constants;

/**
 * Redis使用FastJson序列化
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— Redis序列化器，在 RedisConfig 中被装配到 RedisTemplate。
 * 【为什么需要】Spring默认用JDK序列化存Redis，内容是不可读的字节串且要求实体实现Serializable；
 * 改用FastJson2后，Redis里存的是可读JSON文本，并且不限定实体实现Serializable接口。
 * <p>
 * 【使用方】业务代码不直接用它，而是通过若依封装工具 RedisCache（redisCache.setCacheObject/getCacheObject）间接使用。
 *
 * @param <T> 序列化的目标类型
 * @author ruoyi
 */
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T>
{
    /** 序列化编码：UTF-8，保证中文不乱码 */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * AutoType安全白名单过滤器：反序列化时只允许 Constants.JSON_WHITELIST_STR（com.ruoyi包）下的类进行自动类型识别，
     * 防止FastJson历史上著名的AutoType反序列化漏洞被利用（恶意构造任意类）
     */
    static final Filter AUTO_TYPE_FILTER = JSONReader.autoTypeFilter(Constants.JSON_WHITELIST_STR);

    /** 目标类型的Class对象，反序列化时用于确定转换为什么类型 */
    private Class<T> clazz;

    public FastJson2JsonRedisSerializer(Class<T> clazz)
    {
        super();
        this.clazz = clazz;
    }

    /**
     * 序列化：Java对象 → JSON字符串 → UTF-8字节数组存入Redis
     * <p>
     * WriteClassName：JSON中写入 @type 全限定类名，反序列化时才能还原成原始Java类型（如LoginUser）
     */
    @Override
    public byte[] serialize(T t) throws SerializationException
    {
        if (t == null)
        {
            return new byte[0];
        }
        return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    /**
     * 反序列化：Redis字节数组 → JSON字符串 → Java对象
     * <p>
     * 经过AUTO_TYPE_FILTER白名单校验，非若依包下的类直接拒绝，保证安全
     */
    @Override
    public T deserialize(byte[] bytes) throws SerializationException
    {
        if (bytes == null || bytes.length <= 0)
        {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);

        return JSON.parseObject(str, clazz, AUTO_TYPE_FILTER);
    }
}
