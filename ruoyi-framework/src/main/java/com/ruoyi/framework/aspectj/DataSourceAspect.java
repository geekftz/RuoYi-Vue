package com.ruoyi.framework.aspectj;

import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.datasource.DynamicDataSourceContextHolder;

/**
 * 多数据源切换切面
 * <p>
 * 通过 AOP 拦截带有 @DataSource 注解的方法或类，在方法执行前切换到指定的数据源，
 * 方法执行完成后自动清理数据源标识，恢复为默认数据源。
 * </p>
 * <p>
 * 使用 @Order(1) 确保此切面在其他切面之前执行，保证数据源在事务开启前就切换完成。
 * </p>
 * <p>
 * 典型场景：某些查询需要走从库（只读），或某些业务需要访问不同数据库。
 * 使用方式：在方法或类上添加 @DataSource(DataSourceType.SLAVE)
 * </p>
 *
 * @author ruoyi
 */
@Aspect
@Order(1)
@Component
public class DataSourceAspect
{
    /** 日志记录器 */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 切面点定义：匹配带有 @DataSource 注解的方法或类
     */
    @Pointcut("@annotation(com.ruoyi.common.annotation.DataSource)"
            + "|| @within(com.ruoyi.common.annotation.DataSource)")
    public void dsPointCut()
    {

    }

    /**
     * 环绕通知：切换数据源 → 执行目标方法 → 清理数据源
     * <p>
     * 执行流程：
     * 1. 从方法或类上获取 @DataSource 注解
     * 2. 将注解指定的数据源类型（MASTER/SLAVE）存入 ThreadLocal
     * 3. 执行目标方法（此时MyBatis会从ThreadLocal读取数据源类型）
     * 4. 无论成功或异常，都在 finally 中清理 ThreadLocal，恢复为默认数据源
     * </p>
     *
     * @param point 切点
     * @return 目标方法返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable
    {
        DataSource dataSource = getDataSource(point);

        if (StringUtils.isNotNull(dataSource))
        {
            // 将数据源类型存入ThreadLocal，DynamicDataSource会从此处获取当前使用的数据源
            DynamicDataSourceContextHolder.setDataSourceType(dataSource.value().name());
        }

        try
        {
            return point.proceed();
        }
        finally
        {
            // 方法执行完毕后清理ThreadLocal，防止数据源泄漏影响后续请求
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * 获取需要切换的数据源注解
     * <p>
     * 优先从方法上获取 @DataSource 注解，如果方法上没有则从类上获取。
     * 支持方法级和类级两种粒度的数据源配置。
     * </p>
     *
     * @param point 切点
     * @return 数据源注解，未配置时返回null
     */
    public DataSource getDataSource(ProceedingJoinPoint point)
    {
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 优先从方法上查找注解
        DataSource dataSource = AnnotationUtils.findAnnotation(signature.getMethod(), DataSource.class);
        if (Objects.nonNull(dataSource))
        {
            return dataSource;
        }
        // 方法上没有则从类上查找
        return AnnotationUtils.findAnnotation(signature.getDeclaringType(), DataSource.class);
    }
}
