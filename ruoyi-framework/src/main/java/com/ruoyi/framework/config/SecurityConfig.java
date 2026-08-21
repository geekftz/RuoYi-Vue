package com.ruoyi.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;
import com.ruoyi.framework.config.properties.PermitAllUrlProperties;
import com.ruoyi.framework.security.filter.JwtAuthenticationTokenFilter;
import com.ruoyi.framework.security.handle.AuthenticationEntryPointImpl;
import com.ruoyi.framework.security.handle.LogoutSuccessHandlerImpl;

/**
 * spring security配置
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— 整个系统安全体系的"总开关"。
 * 在请求链路中处于最前置位置：所有HTTP请求到达Controller之前，都必须先经过这里配置的安全过滤器链。
 * <p>
 * 【核心职责】
 * 1. 定义安全过滤器链（SecurityFilterChain）：哪些URL匿名可访问、哪些必须登录认证；
 * 2. 注册JWT认证过滤器：解析前端请求头中的Token，识别登录用户身份；
 * 3. 注册认证管理器（AuthenticationManager）：登录接口校验账号密码的入口；
 * 4. 注册密码加密器（BCryptPasswordEncoder）：用户密码的加密存储与比对；
 * 5. 关闭Session、禁用CSRF：前后端分离 + Token无状态认证的标准配置。
 * <p>
 * 【对系统的影响】本类直接决定全站接口的放行/拦截规则，配置错误会导致接口全部401或产生安全漏洞。
 *
 * @author ruoyi
 */
// 【注解专项】@EnableMethodSecurity：开启Spring Security方法级权限控制。
// prePostEnabled = true 启用 @PreAuthorize 注解（Controller上的 @PreAuthorize("@ss.hasPermi('xxx')") 全靠它生效）；
// securedEnabled = true 启用 @Secured。若不加此注解，所有权限注解都不会被拦截校验
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
// 【注解专项】@Configuration：声明为Spring配置类，启动时优先加载，内部@Bean方法的返回值会注册到Spring容器
@Configuration
public class SecurityConfig
{
    /**
     * 自定义用户认证逻辑
     * <p>
     * 实际注入的是 UserDetailsServiceImpl（framework/security包），
     * 登录认证时由它根据用户名查询 sys_user 等表，组装出含角色权限的 LoginUser。
     * 【注解专项】@Autowired：Spring按类型自动装配容器中的Bean，框架内最常用的依赖注入方式
     */
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * 认证失败处理类
     * <p>
     * 当请求未携带有效Token却访问需认证接口时触发，
     * 统一向前端返回 {"code":401,"msg":"请求访问：xxx，认证失败，无法访问系统资源"}，
     * 前端axios响应拦截器收到401后清除本地Token并跳转登录页
     */
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    /**
     * 退出处理类
     * <p>
     * 前端调用 /logout 退出登录成功后的回调：删除Redis中该用户的Token缓存（登录态即刻失效），
     * 并记录"退出"操作日志，返回 {"msg":"退出成功","code":200}
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * token认证过滤器
     * <p>
     * 除放行URL外，每个请求最先经过它：从请求头 Authorization 解析JWT
     * → 用Token中的uuid查Redis拿到LoginUser → 塞进SecurityContextHolder，
     * 后续Controller里 SecurityUtils.getLoginUser() 能拿到当前用户，靠的就是这一步
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;
    
    /**
     * 跨域过滤器
     * <p>
     * 在 ResourcesConfig 中定义为Bean，允许浏览器跨域调用后端接口
     * （前端Vue开发服务器与后端不同端口，跨域必须放行）
     */
    @Autowired
    private CorsFilter corsFilter;

    /**
     * 允许匿名访问的地址
     * <p>
     * 扫描所有标注了若依自定义 @Anonymous 注解的接口路径，启动时收集，下方统一放行，
     * 避免每加一个免登录接口都要改本类
     */
    @Autowired
    private PermitAllUrlProperties permitAllUrl;

    /**
     * 身份验证实现
     * <p>
     * 登录链路核心入口：SysLoginService.login() 调用 authenticationManager.authenticate() 后，
     * 内部流程 → userDetailsService 按用户名查库组装LoginUser → BCrypt比对密码 →
     * 成功返回Authentication对象，失败抛出 BadCredentialsException 等异常（被全局异常/登录服务捕获转为友好提示）
     */
    @Bean
    public AuthenticationManager authenticationManager()
    {
        // 基于数据库DAO的认证提供者：把"查用户"与"比对密码"分别委托给下面两个组件
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    /**
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
    {
        return httpSecurity
            // CSRF禁用，因为不使用session
            // 【原理】CSRF攻击依赖浏览器自动携带Cookie中的Session凭证；若改用请求头Token认证，天然免疫CSRF，故关闭
            .csrf(csrf -> csrf.disable())
            // 禁用HTTP响应标头
            // 关闭缓存控制响应头；frameOptions=sameOrigin 表示页面只允许被同源的iframe嵌套（防点击劫持）
            .headers((headersCustomizer) -> {
                headersCustomizer.cacheControl(cache -> cache.disable()).frameOptions(options -> options.sameOrigin());
            })
            // 认证失败处理类
            // 未认证访问受保护资源时，交给上面注入的 unauthorizedHandler 返回统一401 JSON
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            // 基于token，所以不需要session
            // STATELESS：服务端不创建HttpSession，登录态完全由Token+Redis承载，支持水平扩容
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 注解标记允许匿名访问的url
            // 遍历所有 @Anonymous 注解标注的接口路径并放行（permitAll = 无需登录即可访问）
            .authorizeHttpRequests((requests) -> {
                permitAllUrl.getUrls().forEach(url -> requests.antMatchers(url).permitAll());
                // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                requests.antMatchers("/login", "/register", "/captchaImage").permitAll()
                    // 静态资源，可匿名访问
                    .antMatchers(HttpMethod.GET, "/", "/*.html", "/**/*.html", "/**/*.css", "/**/*.js", "/profile/**").permitAll()
                    // Swagger接口文档、Druid监控页，可匿名访问（生产环境建议关闭或加IP白名单）
                    .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/*/api-docs", "/druid/**").permitAll()
                    // 除上面外的所有请求全部需要鉴权认证
                    // 【关键】兜底规则：不在放行清单里的接口一律要求已认证，否则走 unauthorizedHandler 返回401
                    .anyRequest().authenticated();
            })
            // 添加Logout filter
            // 拦截 /logout 请求，退出成功后交给 logoutSuccessHandler 清理Redis登录态
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
            // 添加JWT filter
            // 【过滤器顺序】JWT过滤器放在Spring Security用户名密码认证过滤器之前，确保先识别Token身份
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            // 添加CORS filter
            // 跨域过滤器优先级最高：先于JWT过滤器和Logout过滤器执行，保证预检OPTIONS请求不被拦截
            .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
            .addFilterBefore(corsFilter, LogoutFilter.class)
            .build();
    }

    /**
     * 强散列哈希加密实现
     * <p>
     * BCrypt算法：每次加密自动生成随机盐，相同密码两次加密结果不同，抗彩虹表攻击。
     * 使用场景：SysUserServiceImpl 新增/重置密码时 SecurityUtils.encryptPassword() 加密入库；
     * 登录时用 matches() 比对明文与库中密文，全程不可逆、不解密
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
