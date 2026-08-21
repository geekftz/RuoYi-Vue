package com.ruoyi.framework.config;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.ServletUtils;

/**
 * 服务相关配置
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— 获取服务自身访问地址的工具型组件。
 * 【使用场景】生成需要完整URL的业务数据时使用，如代码生成器预览、Swagger文档的服务地址展示等。
 *
 * @author ruoyi
 */
@Component
public class ServerConfig
{
    /**
     * 获取完整的请求路径，包括：域名，端口，上下文访问路径
     * <p>
     * 例如前端请求 http://localhost:8080/system/user/list，本方法返回 http://localhost:8080/
     * 
     * @return 服务地址
     */
    public String getUrl()
    {
        // 通过 ServletUtils 从当前线程的 RequestContextHolder 中取出本次请求的 request 对象
        HttpServletRequest request = ServletUtils.getRequest();
        return getDomain(request);
    }

    /**
     * 从完整请求URL中截取"协议+域名+端口+上下文路径"部分
     * <p>
     * 原理：完整URL 去掉 请求URI 部分，再拼上 contextPath，
     * 如 http://host:8080/system/user/list 去掉 /system/user/list → http://host:8080
     *
     * @param request 当前HTTP请求
     * @return 服务域名地址
     */
    public static String getDomain(HttpServletRequest request)
    {
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
    }
}
