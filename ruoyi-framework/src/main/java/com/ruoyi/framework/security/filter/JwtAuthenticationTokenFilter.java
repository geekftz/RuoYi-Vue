package com.ruoyi.framework.security.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.TokenService;

/**
 * JWT 认证令牌过滤器
 * <p>
 * 继承 Spring Security 的 OncePerRequestFilter，确保每个请求只过滤一次。
 * 这是整个认证体系的核心过滤器，配置在 Spring Security 过滤链之前。
 * </p>
 * <p>
 * 工作流程：
 * 1. 从请求头中解析JWT令牌，从Redis获取LoginUser
 * 2. 若获取到用户且Spring Security上下文中未设置认证信息，则：
 *    a. 验证令牌有效期，不足20分钟自动续期
 *    b. 构建 UsernamePasswordAuthenticationToken 注入SecurityContext
 *    c. 后续接口即可通过 SecurityUtils.getLoginUser() 获取当前登录用户
 * 3. 无论是否认证成功，都继续执行后续过滤器链
 * </p>
 *
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    /** 令牌服务 */
    @Autowired
    private TokenService tokenService;

    /**
     * 核心过滤方法：每个HTTP请求都会经过此方法
     * <p>
     * 1. 从请求中解析JWT并从Redis获取LoginUser
     * 2. 若用户存在且SecurityContext中尚无认证信息（未登录状态），则注入认证信息
     * 3. 调用verifyToken检查并自动续期
     * 4. 继续过滤器链
     * </p>
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param chain    过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        // 从请求头中解析JWT，从Redis中获取登录用户信息
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication()))
        {
            // 验证令牌有效期，距过期不足20分钟时自动续期
            tokenService.verifyToken(loginUser);
            // 构建Spring Security认证令牌（包含用户信息和权限列表）
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            // 设置认证详情（远程IP、SessionId等）
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 注入SecurityContext，后续接口可通过SecurityUtils获取当前用户
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        // 继续执行后续过滤器和请求处理
        chain.doFilter(request, response);
    }
}
