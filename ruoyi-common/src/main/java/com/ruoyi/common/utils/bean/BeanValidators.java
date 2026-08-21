package com.ruoyi.common.utils.bean;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

/**
 * bean对象属性验证
 * <p>
 * 【能力】编程式触发JSR-303 Bean Validation校验（等价于Controller中@Valid注解的效果，但可以在任意代码位置调用）。
 * 【使用场景】Service层需要手动校验对象属性合法性时（不走Controller的@Valid自动校验），调用此方法。
 * 【使用示例】
 * <pre>
 * @Autowired Validator validator;
 * BeanValidators.validateWithException(validator, sysUser); // 校验失败抛ConstraintViolationException
 * </pre>
 * 
 * @author ruoyi
 */
public class BeanValidators
{
    /**
     * 校验对象属性，发现违规立即抛ConstraintViolationException
     * 
     * @param validator JSR-303校验器（Spring自动注入）
     * @param object 待校验的Bean对象
     * @param groups 校验分组（可选，用于不同场景校验不同规则）
     * @throws ConstraintViolationException 校验失败时抛出，包含所有违规信息
     */
    public static void validateWithException(Validator validator, Object object, Class<?>... groups)
            throws ConstraintViolationException
    {
        // 执行JSR-303校验，返回所有违规项
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty())
        {
            // 有违规项则抛出异常，由全局异常处理器统一捕获并返回前端
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}
