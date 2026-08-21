package com.ruoyi.common.utils.spring;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;

/**
 * spring工具类 方便在非spring管理环境中获取bean
 * <p>
 * 【架构位置】common/utils/spring，Spring容器访问工具。
 * 【解决的问题】普通Java类（如工具类、定时任务、第三方回调）无法使用@Autowired注入Bean，
 * 因为@Autowired只在Spring管理的Bean中生效。本类通过实现BeanFactoryPostProcessor接口，
 * 在Spring启动时就把BeanFactory引用存到静态变量中，之后任何地方都可以通过SpringUtils.getBean()获取Bean。
 * 【核心方法】
 * - getBean(name/class)：按名称或类型获取Spring容器中的Bean
 * - getAopProxy(invoker)：获取AOP代理对象（用于同类内部方法调用时触发切面）
 * - getActiveProfile()：获取当前激活的环境配置（dev/test/prod）
 * - getRequiredProperty(key)：读取配置文件中的值
 * 【使用示例】
 * <pre>
 * // 在非Spring管理的类中获取Bean
 * RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
 * </pre>
 * 
 * @author ruoyi
 */
@Component
public final class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware 
{
    /** Spring应用上下文环境（BeanFactoryPostProcessor回调时注入） */
    private static ConfigurableListableBeanFactory beanFactory;

    /** Spring应用上下文（ApplicationContextAware回调时注入） */
    private static ApplicationContext applicationContext;

    /**
     * BeanFactoryPostProcessor接口回调：Spring启动时将BeanFactory存入静态变量
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException 
    {
        SpringUtils.beanFactory = beanFactory;
    }

    /**
     * ApplicationContextAware接口回调：Spring启动时将ApplicationContext存入静态变量
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException 
    {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 获取对象（按Bean名称）
     * 【若依高频用法】SpringUtils.getBean("sysUserService")
     *
     * @param name Bean名称
     * @return Object 一个以所给名字注册的bean的实例
     * @throws org.springframework.beans.BeansException
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException
    {
        return (T) beanFactory.getBean(name);
    }

    /**
     * 获取类型为requiredType的对象（按类型）
     * 【若依高频用法】SpringUtils.getBean(RedisCache.class)
     *
     * @param clz Bean类型
     * @return Bean实例
     * @throws org.springframework.beans.BeansException
     *
     */
    public static <T> T getBean(Class<T> clz) throws BeansException
    {
        T result = (T) beanFactory.getBean(clz);
        return result;
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String name)
    {
        return beanFactory.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     *
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     *
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     *
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.getAliases(name);
    }

    /**
     * 获取aop代理对象（解决同类内部方法调用时AOP失效的经典问题）
     * 【使用场景】同一个Service类中方法A调用方法B，如果直接this.B()调用不会触发B上的AOP切面（如@Transactional），
     * 通过SpringUtils.getAopProxy(this).B()调用则走代理，切面正常生效。
     * 
     * @param invoker 原始对象（通常是this）
     * @return AOP代理对象，如果不是代理则返回原对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker)
    {
        // AopContext.currentProxy()获取当前线程的AOP代理（需要开启exposeProxy=true）
        Object proxy = AopContext.currentProxy();
        if (((Advised) proxy).getTargetSource().getTargetClass() == invoker.getClass())
        {
            return (T) proxy;
        }
        return invoker;
    }

    /**
     * 获取当前的环境配置，无配置返回null
     *
     * @return 当前的环境配置
     */
    public static String[] getActiveProfiles()
    {
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取当前的环境配置，当有多个环境配置时，只获取第一个
     *
     * @return 当前的环境配置
     */
    public static String getActiveProfile()
    {
        final String[] activeProfiles = getActiveProfiles();
        return StringUtils.isNotEmpty(activeProfiles) ? activeProfiles[0] : null;
    }

    /**
     * 获取配置文件中的值
     *
     * @param key 配置文件的key
     * @return 当前的配置文件的值
     *
     */
    public static String getRequiredProperty(String key)
    {
        return applicationContext.getEnvironment().getRequiredProperty(key);
    }
}
