package com.ruoyi.framework.config.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.ruoyi.common.annotation.Anonymous;

/**
 * 设置Anonymous注解允许匿名访问的url
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— 配合若依自定义 @Anonymous 注解的"免登录接口自动收集器"。
 * 【工作机制】Spring容器启动完成后（afterPropertiesSet），扫描所有Controller的方法与类，
 * 凡标注 @Anonymous 的，把其URL收集进 urls 列表；SecurityConfig 的过滤器链启动时读取该列表统一 permitAll 放行。
 * 【好处】新增免登录接口只需在方法上加 @Anonymous，不用改SecurityConfig，避免安全配置硬编码。
 *
 * @author ruoyi
 */
@Configuration
public class PermitAllUrlProperties implements InitializingBean, ApplicationContextAware
{
    /** 路径变量正则：匹配URL中的 {xxx} 占位符（如 /user/{id}） */
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    /** Spring应用上下文，用于拿到所有已注册的Controller映射信息 */
    private ApplicationContext applicationContext;

    /** 收集到的所有允许匿名访问的URL列表，供 SecurityConfig 读取放行 */
    private List<String> urls = new ArrayList<>();

    /** 通配符：将URL中的路径变量 {id} 替换为 *，使 /user/1、/user/2 都能匹配放行规则 */
    public String ASTERISK = "*";

    /**
     * InitializingBean接口方法：Bean初始化完成后由Spring自动回调，在此执行URL收集
     */
    @Override
    public void afterPropertiesSet()
    {
        // RequestMappingHandlerMapping 持有所有 @RequestMapping 映射信息（URL → Controller方法）
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();

        map.keySet().forEach(info -> {
            HandlerMethod handlerMethod = map.get(info);

            // 获取方法上边的注解 替代path variable 为 *
            Anonymous method = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
            Optional.ofNullable(method).ifPresent(anonymous -> Objects.requireNonNull(info.getPatternsCondition().getPatterns())
                    .forEach(url -> urls.add(RegExUtils.replaceAll(url, PATTERN, ASTERISK))));

            // 获取类上边的注解, 替代path variable 为 *
            // 类上标注 @Anonymous 表示该Controller所有方法都免登录
            Anonymous controller = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);
            Optional.ofNullable(controller).ifPresent(anonymous -> Objects.requireNonNull(info.getPatternsCondition().getPatterns())
                    .forEach(url -> urls.add(RegExUtils.replaceAll(url, PATTERN, ASTERISK))));
        });
    }

    /**
     * ApplicationContextAware接口方法：Spring创建Bean时自动注入应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        this.applicationContext = context;
    }

    public List<String> getUrls()
    {
        return urls;
    }

    public void setUrls(List<String> urls)
    {
        this.urls = urls;
    }
}
