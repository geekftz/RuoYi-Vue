package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.OperatorType;

/**
 * 自定义操作日志记录注解
 * <p>
 * 【作用】标在Controller方法上，自动记录"谁在什么时间用什么IP调了什么接口、传了什么参数、成功还是失败"，
 *         写入sys_oper_log表，后台"操作日志"页面可查。
 * 【处理链路】LogAspect切面拦截本注解 → 方法执行前后记录耗时、捕获请求参数/返回结果 →
 *             组装SysOperLog对象 → AsyncManager异步线程写入数据库（不阻塞主请求）。
 * 【使用示例】@Log(title = "用户管理", businessType = BusinessType.INSERT) 标在新增用户接口上。
 * 【前端联动】前端"系统监控→操作日志"菜单展示的正是本注解产生的数据。
 * 
 * @author ruoyi
 *
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log
{
    /**
     * 模块（如"用户管理"，显示在操作日志列表的"系统模块"列）
     */
    public String title() default "";

    /**
     * 功能（BusinessType枚举：INSERT/UPDATE/DELETE/EXPORT等，显示在"操作类型"列，同时决定日志图标）
     */
    public BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别
     */
    public OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数（默认true；密码修改等敏感接口应设false，防止密码明文落库：
     *  如@Log(title="重置密码", businessType=UPDATE, isSaveRequestData=false)）
     */
    public boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    public boolean isSaveResponseData() default true;

    /**
     * 排除指定的请求参数（如@Log(..., excludeParamNames={"password"})，只排除个别敏感字段，其余参数仍记录）
     */
    public String[] excludeParamNames() default {};
}
