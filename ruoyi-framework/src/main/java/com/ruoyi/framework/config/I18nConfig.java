package com.ruoyi.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import com.ruoyi.common.constant.Constants;

/**
 * 资源文件配置加载
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— 国际化（i18n）配置。
 * 【作用】让后端返回的提示信息支持多语言：语言文案定义在 ruoyi-admin/resources/i18n/messages.properties 中，
 * 代码里通过 MessageUtils.message("user.login.success") 获取对应语言的文案。
 * 【前端联动】前端请求可携带 ?lang=en_US 参数切换语言，下方拦截器负责识别。
 *
 * @author ruoyi
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer
{
    /**
     * 语言解析器：决定"当前请求用哪种语言"
     * <p>
     * SessionLocaleResolver 将用户语言选择存在Session中；默认语言来自 Constants.DEFAULT_LOCALE（中文简体 zh_CN）
     */
    @Bean
    public LocaleResolver localeResolver()
    {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        // 默认语言
        slr.setDefaultLocale(Constants.DEFAULT_LOCALE);
        return slr;
    }

    /**
     * 语言切换拦截器：每个请求检查URL中是否带 lang 参数（如 ?lang=en_US），有则切换当前语言
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor()
    {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        // 参数名
        lci.setParamName("lang");
        return lci;
    }

    /**
     * 把语言切换拦截器注册到Spring MVC拦截器链，对所有请求生效
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
