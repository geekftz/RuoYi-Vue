package com.ruoyi.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.text.Convert;

/**
 * Servlet 客户端工具类
 * <p>
 * 封装了从当前线程获取 HttpServletRequest/HttpServletResponse 的方法，
 * 以及从请求中获取参数、判断 Ajax 请求、URL 编解码等常用操作。
 * </p>
 * <p>
 * 核心原理：通过 RequestContextHolder.getRequestAttributes() 获取当前线程的请求上下文，
 * 因此只能在有 HTTP 请求上下文的环境中使用（Controller、Filter 等）。
 * </p>
 * <p>
 * 【若依中的典型用法】
 * · 过滤器/拦截器里直接返回JSON：ServletUtils.renderString(response, json)（如认证失败401、重复提交拦截）；
 * · 非Controller层取请求参数：ServletUtils.getParameter("xxx")；
 * · 全局异常处理器判断请求类型决定是否返回JSON。
 * </p>
 *
 * @author ruoyi
 */
public class ServletUtils
{
    /**
     * 获取String参数
     */
    public static String getParameter(String name)
    {
        return getRequest().getParameter(name);
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name, String defaultValue)
    {
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name)
    {
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue)
    {
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name)
    {
        return Convert.toBool(getRequest().getParameter(name));
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue)
    {
        return Convert.toBool(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String[]> getParams(ServletRequest request)
    {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String> getParamMap(ServletRequest request)
    {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet())
        {
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return params;
    }

    /**
     * 获取当前线程的 HttpServletRequest
     *
     * @return HTTP请求对象
     */
    public static HttpServletRequest getRequest()
    {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取当前线程的 HttpServletResponse
     *
     * @return HTTP响应对象
     */
    public static HttpServletResponse getResponse()
    {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取session
     */
    public static HttpSession getSession()
    {
        return getRequest().getSession();
    }

    /**
     * 获取当前线程的 ServletRequestAttributes
     * <p>
     * 通过 Spring 的 RequestContextHolder 获取当前请求上下文，
     * 这是从非 Controller 代码（如 Service、工具类）中访问请求对象的唯一方式。
     * </p>
     *
     * @return ServletRequestAttributes 请求属性对象
     */
    public static ServletRequestAttributes getRequestAttributes()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 将字符串渲染到客户端（直接输出 JSON 响应）
     * <p>
     * 用于在过滤器或拦截器中直接返回 JSON 响应，
     * 例如：退出登录成功后返回 JSON、权限校验失败后返回 JSON。
     * </p>
     *
     * @param response HTTP响应对象
     * @param string   待渲染的字符串（通常为JSON）
     */
    public static void renderString(HttpServletResponse response, String string)
    {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否为 Ajax 异步请求
     * <p>
     * 通过多种方式判断：
     * - accept 头包含 application/json
     * - X-Requested-With 头为 XMLHttpRequest
     * - URI 以 .json 或 .xml 结尾
     * - 请求参数 __ajax 为 json 或 xml
     * </p>
     *
     * @param request HTTP请求对象
     * @return true=Ajax请求，false=非Ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request)
    {
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains("application/json"))
        {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest"))
        {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.inStringIgnoreCase(uri, ".json", ".xml"))
        {
            return true;
        }

        String ajax = request.getParameter("__ajax");
        return StringUtils.inStringIgnoreCase(ajax, "json", "xml");
    }

    /**
     * 内容编码
     * 
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str)
    {
        try
        {
            return URLEncoder.encode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 内容解码
     * 
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str)
    {
        try
        {
            return URLDecoder.decode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }
}
