package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解防止表单重复提交
 * <p>
 * 【作用】标在Controller方法上，拦截短时间内的重复请求（双击、网络重试、恶意刷新导致的重复下单/重复新增）。
 * 【处理链路】RepeatSubmitInterceptor（实现了HandlerInterceptor）在Controller执行前拦截：
 *             把本次请求的URL+参数+会话ID做key存Redis，interval毫秒内相同key再次出现 → 抛ServiceException拒绝。
 * 【使用示例】@RepeatSubmit(interval = 5000, message = "不允许重复提交") —— 5秒内相同请求直接拒绝。
 * 【与前端配合】属于后端兜底，前端按钮点击后loading禁用是第一道防线。
 * 
 * @author ruoyi
 *
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit
{
    /**
     * 间隔时间(ms)，小于此时间视为重复提交
     */
    public int interval() default 5000;

    /**
     * 提示消息
     */
    public String message() default "不允许重复提交，请稍候再试";
}
