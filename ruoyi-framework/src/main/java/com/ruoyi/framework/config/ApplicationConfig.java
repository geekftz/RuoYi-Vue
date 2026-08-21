package com.ruoyi.framework.config;

import java.util.TimeZone;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 程序注解配置
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— 应用级基础配置，启动时最先加载的一批配置类。
 * 负责三项全局能力：开启AOP代理暴露、扫描MyBatis Mapper接口、统一JSON序列化时区。
 *
 * @author ruoyi
 */
@Configuration
// 表示通过aop框架暴露该代理对象,AopContext能够访问
// 【深入】exposeProxy=true 后，代码中可通过 AopContext.currentProxy() 拿到当前类的代理对象，
// 解决"同类中方法自调用导致AOP（如@Transactional事务）失效"的经典问题
@EnableAspectJAutoProxy(exposeProxy = true)
// 指定要扫描的Mapper类的包的路径
// 【深入】com.ruoyi.**.mapper 包下所有Mapper接口会被MyBatis自动生成动态代理实现并注册到Spring容器，
// ServiceImpl中 @Resource 注入即可直接调用，全程无需手写实现类，SQL来自同名XML文件
@MapperScan("com.ruoyi.**.mapper")
public class ApplicationConfig
{
    /**
     * 时区配置
     * <p>
     * 定制Jackson（Spring默认JSON序列化器）的时区为操作系统默认时区，
     * 保证后端返回给前端的 Date 类型字段按本地时区格式化，避免前后端时间差8小时的问题
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization()
    {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }
}
