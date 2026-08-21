package com.ruoyi.common.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.enums.HttpMethod;

/**
 * 防止XSS攻击的过滤器
 * <p>
 * 【架构位置】filter包，在ResourcesConfig中注册为Servlet过滤器。
 * 【能力】对所有POST/PUT请求的请求参数进行XSS（跨站脚本攻击）清洗，
 * 把<script>、onclick等恶意脚本字符转义为无害文本，防止恶意脚本存入数据库后在页面上执行。
 * 【过滤策略】
 * 1. GET/DELETE请求不过滤（只读操作，风险低）
 * 2. excludes白名单URL不过滤（某些接口如富文本编辑器需要保留HTML内容）
 * 3. 其余请求用XssHttpServletRequestWrapper包装，对参数和body统一清洗
 * 【前端联动】用户输入<script>alert(1)</script>提交后，后端存库的是转义后的文本，前端展示时不会执行脚本。
 * 
 * @author ruoyi
 */
public class XssFilter implements Filter
{
    /**
     * 排除链接（不需要XSS过滤的URL，如富文本编辑器提交内容的接口），在FilterConfig中配置
     */
    public List<String> excludes = new ArrayList<>();

    /**
     * 过滤器初始化：读取init-param中配置的excludes参数（逗号分隔的URL列表）
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        String tempExcludes = filterConfig.getInitParameter("excludes");
        if (StringUtils.isNotEmpty(tempExcludes))
        {
            String[] urls = tempExcludes.split(",");
            for (String url : urls)
            {
                excludes.add(url);
            }
        }
    }

    /**
     * 过滤核心逻辑：
     * 1. 先判断是否排除（GET/DELETE/白名单URL直接放行）
     * 2. 否则用XssHttpServletRequestWrapper包装request，后续取参数/读body时自动清洗XSS字符
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        // 检查是否为排除的URL（GET/DELETE/白名单），是则原样放行
        if (handleExcludeURL(req, resp))
        {
            chain.doFilter(request, response);
            return;
        }
        // 包装request，所有取参数/读body操作都会被XSS清洗
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    /**
     * 判断是否排除：GET/DELETE请求不过滤，白名单URL不过滤
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @return true=排除（不清洗），false=需要XSS清洗
     */
    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response)
    {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method))
        {
            return true;
        }
        // 白名单URL匹配（支持Ant风格通配符如/system/notice/**）
        return StringUtils.matches(url, excludes);
    }

    @Override
    public void destroy()
    {

    }
}