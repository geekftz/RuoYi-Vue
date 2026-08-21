package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限过滤注解
 * <p>
 * 【作用】标在Service查询方法上，自动按当前登录用户角色的数据范围（dataScope）给SQL追加过滤条件，
 *         实现"不同角色看到不同范围的数据"（如普通员工只能看本部门用户，领导能看本部门及以下）。
 * 【处理链路】DataScopeAspect切面拦截本注解 → 遍历当前用户角色取dataScope最宽范围 →
 *             拼出AND条件SQL片段 → 存入实体params.dataScope → Mapper XML中${params.dataScope}拼入SQL。
 * 【使用示例】@DataScope(deptAlias = "d", userAlias = "u") 标在selectUserList上，
 *             XML中FROM sys_user u LEFT JOIN sys_dept d ... WHERE ... ${params.dataScope}
 * 【前提】SQL必须联查sys_dept表（别名与deptAlias一致）且实体继承BaseEntity（才有params属性）。
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope
{
    /**
     * 用户表的别名
     */
    public String userAlias() default "";

    /**
     * 部门表的别名（对应XML中sys_dept表的别名，切面拼SQL时用"别名.dept_id"定位部门字段）
     */
    public String deptAlias() default "";

    /**
     * 用户字段名
     */
    public String userField() default "user_id";

    /**
     * 部门字段名
     */
    public String deptField() default "dept_id";

    /**
     * 权限字符（用于多个角色匹配符合要求的权限）默认根据权限注解@ss获取，多个权限用逗号分隔开来
     * <p>
     * 【使用场景】一个用户可能有多个角色，本注解指定只对拥有某权限的角色应用数据范围过滤，避免误伤。
     */
    public String permission() default "";
}
