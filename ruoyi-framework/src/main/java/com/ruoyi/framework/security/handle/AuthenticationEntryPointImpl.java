package com.ruoyi.framework.security.handle;

import java.io.IOException;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 认证失败处理类 返回未授权
 * <p>
 * 【架构位置】ruoyi-framework / security / handle层 —— Spring Security认证异常统一出口。
 * 【触发时机】在 SecurityConfig 中注册为 authenticationEntryPoint：当未携带/携带无效Token的请求
 * 访问了需要认证的接口时，Spring Security抛出 AuthenticationException，最终回调本类 commence()。
 * 【前端联动】返回固定401格式的JSON，前端axios响应拦截器识别 code=401 → 清除本地Token → 跳转登录页。
 *
 * @author ruoyi
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable
{
    private static final long serialVersionUID = -8970718410437077606L;

    /**
     * 认证失败回调：向前端写出401 JSON
     *
     * @param request 当前请求（取URI拼提示语）
     * @param response 响应对象（直接写出JSON，不再进入Controller）
     * @param e Spring Security抛出的认证异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException
    {
        int code = HttpStatus.UNAUTHORIZED;
        // 若依统一返回体 AjaxResult 序列化为：{"msg":"请求访问：xxx，认证失败，无法访问系统资源","code":401}
        String msg = StringUtils.format("请求访问：{}，认证失败，无法访问系统资源", request.getRequestURI());
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
    }
}
