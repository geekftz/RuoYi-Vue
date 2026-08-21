package com.ruoyi.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel注解集
 * <p>
 * 【作用】Java不允许同一字段标注多个同名注解，本注解作为容器解决此限制：一个字段需要拆成多列导出时使用。
 * 【典型示例】SysUser.dept字段：@Excels({@Excel(name="部门名称", targetAttr="deptName"),
 *             @Excel(name="部门负责人", targetAttr="leader")}) —— 一个dept对象拆成两列导出。
 * 
 * @author ruoyi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Excels
{
    public Excel[] value();
}
