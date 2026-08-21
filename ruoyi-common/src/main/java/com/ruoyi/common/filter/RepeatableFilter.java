package com.ruoyi.common.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import com.ruoyi.common.utils.StringUtils;

/**
 * Repeatable 过滤器
 * <p>
 * 【架构位置】过滤器链最前端的"请求体重包装器"，在ResourcesConfig中注册。
 * 【解决的核心问题】HttpServletRequest的请求体（body）流只能读取一次，
 * 但若依有多个环节需要读body：防重复提交拦截器要比对请求参数、@Log切面要记录请求参数，
 * 后续Controller的@RequestBody还要再读一次 → 冲突！
 * 【解决方案】对Content-Type为application/json的请求，用RepeatedlyRequestWrapper把body字节缓存起来，
 * 之后每次getInputStream()都返回新流，实现body可重复读取。
 * 【联动】与RepeatedlyRequestWrapper、SameUrlDataInterceptor（防重提交）、LogAspect（日志切面）配合使用。
 * 
 * @author ruoyi
 */
public class RepeatableFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    /**
     * 过滤逻辑：仅对JSON请求进行包装（表单请求不需要，因为表单参数不走body流）
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        ServletRequest requestWrapper = null;
        // 只包装application/json类型的请求（前端axios默认就是json提交）
        if (request instanceof HttpServletRequest
                && StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE))
        {
            // 包装：构造时立即把body读入内存缓存，后续可重复读
            requestWrapper = new RepeatedlyRequestWrapper((HttpServletRequest) request, response);
        }
        if (null == requestWrapper)
        {
            // 非json请求（如GET、表单提交），原样放行
            chain.doFilter(request, response);
        }
        else
        {
            // json请求：传递包装后的request，下游读到的是可重复读的body
            chain.doFilter(requestWrapper, response);
        }
    }

    @Override
    public void destroy()
    {

    }
}
