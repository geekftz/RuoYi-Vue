package com.ruoyi.common.enums;

import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * 请求方式
 * <p>
 * 【使用者】LogAspect记录操作日志时把请求方法字符串转成枚举存入sys_oper_log.request_method列。
 * 【实现】静态块把所有枚举值注册进Map，resolve()按字符串名查找——比循环values()比对高效。
 * 源自Spring的HttpMethod精简版（Spring自带的在org.springframework.http包，若依为避免依赖冲突自实现）。
 *
 * @author ruoyi
 */
public enum HttpMethod
{
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

    private static final Map<String, HttpMethod> mappings = new HashMap<>(16);

    static
    {
        for (HttpMethod httpMethod : values())
        {
            mappings.put(httpMethod.name(), httpMethod);
        }
    }

    @Nullable
    public static HttpMethod resolve(@Nullable String method)
    {
        return (method != null ? mappings.get(method) : null);
    }

    public boolean matches(String method)
    {
        return (this == resolve(method));
    }
}
