package com.ruoyi.common.xss;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义xss校验注解
 * <p>
 * 【架构位置】common/xss，JSR-303自定义校验注解，与XssValidator配合使用。
 * 【能力】标记在Entity的getter方法上，校验该字段值是否包含HTML/脚本标签，防止XSS攻击。
 * 【使用场景】标注在SysUser的userName、nickName等字段的getter上（见SysUser源码），
 * 配合Controller的@Validated注解自动触发校验。
 * 【与XssFilter的区别】XssFilter是全局请求级清洗（所有请求参数都清洗），
 * @Xss注解是字段级精确校验（只检查标注的字段，不修改值，发现HTML直接报错拒绝）。
 * 【触发时机】Controller方法参数带@Validated注解时，Spring自动调用XssValidator校验。
 * 
 * @author ruoyi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
@Constraint(validatedBy = { XssValidator.class }) // 指定校验逻辑由XssValidator实现
public @interface Xss
{
    /** 校验失败时的错误提示信息 */
    String message()

    default "不允许任何脚本运行";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
