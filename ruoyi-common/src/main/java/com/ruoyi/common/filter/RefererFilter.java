package com.ruoyi.common.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 防盗链过滤器
 * <p>
 * 【能力】检查HTTP请求的Referer头，只允许白名单域名来源的请求访问，防止其他网站盗用本站资源接口。
 * 【适用场景】保护静态资源（图片/视频/下载文件）不被外部网站直接引用盗链，消耗服务器带宽。
 * 【注意】若依默认并未在web.xml或SecurityConfig中启用本过滤器，属于备用组件；
 * 需要时在FilterConfig中通过init-param配置allowedDomains参数（逗号分隔的域名列表）。
 * 【局限】Referer头可被客户端伪造或浏览器策略移除，防盗链只能防君子不防小人，
 * 高安全场景应使用签名URL、Token鉴权等方案。
 * 
 * @author ruoyi
 */
public class RefererFilter implements Filter
{
    /**
     * 允许的域名列表（如 ["ruoyi.vip", "localhost"]），在FilterConfig的init-param中配置
     */
    public List<String> allowedDomains;

    /**
     * 过滤器初始化：读取web.xml/注册时配置的allowedDomains参数，拆分为域名列表
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        String domains = filterConfig.getInitParameter("allowedDomains");
        this.allowedDomains = Arrays.asList(domains.split(","));
    }

    /**
     * 过滤核心逻辑：
     * 1. 取出请求头Referer（标识请求从哪个页面发起）
     * 2. Referer为空直接拒绝（403）
     * 3. Referer包含任一白名单域名则放行，否则拒绝
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Referer头：浏览器自动携带，标识当前请求的来源页面URL
        String referer = req.getHeader("Referer");

        // 如果Referer为空，拒绝访问
        if (referer == null || referer.isEmpty())
        {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Referer header is required");
            return;
        }

        // 检查Referer是否在允许的域名列表中
        boolean allowed = false;
        for (String domain : allowedDomains)
        {
            // contains模糊匹配：Referer是完整URL，只要包含白名单域名即认为合法
            if (referer.contains(domain))
            {
                allowed = true;
                break;
            }
        }

        // 根据检查结果决定是否放行
        if (allowed)
        {
            chain.doFilter(request, response);
        }
        else
        {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Referer '" + referer + "' is not allowed");
        }
    }

    @Override
    public void destroy()
    {

    }
}