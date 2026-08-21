package com.ruoyi;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * web容器中进行部署
 * <p>
 * 【架构位置】ruoyi-admin —— 外部Tomcat部署支持类。
 * 【什么时候用到】默认打jar包用内嵌Tomcat启动，本类不起作用；
 * 若需要打war包部署到外部Tomcat（pom.xml中打包方式改为war），外部容器启动时会通过本类引导Spring Boot应用。
 * 原理：Servlet3.0规范下Tomcat自动发现 SpringBootServletInitializer 的子类并完成应用初始化。
 *
 * @author ruoyi
 */
public class RuoYiServletInitializer extends SpringBootServletInitializer
{
    /**
     * 告诉外部容器：用 RuoYiApplication 作为配置源启动整个Spring应用
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        return application.sources(RuoYiApplication.class);
    }
}
