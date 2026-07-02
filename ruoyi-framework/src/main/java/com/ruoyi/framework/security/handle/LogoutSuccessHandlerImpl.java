package com.ruoyi.framework.security.handle;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.web.service.TokenService;

/**
 * 自定义退出登录处理器
 * <p>
 * 实现 Spring Security 的 LogoutSuccessHandler 接口，在用户退出登录时执行清理操作。
 * </p>
 * <p>
 * 退出流程：
 * 1. 从请求中解析JWT获取当前登录用户信息
 * 2. 删除Redis中的用户缓存（使用户后续请求无法通过认证）
 * 3. 异步记录退出登录日志到sys_logininfor表
 * 4. 返回退出成功的JSON响应给前端
 * </p>
 *
 * @author ruoyi
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    /** 令牌服务 */
    @Autowired
    private TokenService tokenService;

    /**
     * 用户退出登录处理
     * <p>
     * Spring Security在检测到退出请求（/logout）后自动调用此方法。
     * </p>
     *
     * @param request        HTTP请求
     * @param response       HTTP响应
     * @param authentication 认证信息
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        // 从请求中解析JWT获取当前登录用户
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            String userName = loginUser.getUsername();
            // 删除Redis中的用户缓存，使该JWT令牌立即失效
            tokenService.delLoginUser(loginUser.getToken());
            // 异步记录退出登录日志到sys_logininfor表
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
        }
        // 返回退出成功的JSON响应给前端
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));
    }
}
