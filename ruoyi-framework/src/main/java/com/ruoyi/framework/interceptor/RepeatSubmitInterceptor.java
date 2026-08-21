package com.ruoyi.framework.interceptor;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ServletUtils;

/**
 * 防止重复提交拦截器（抽象模板类）
 * <p>
 * 【架构位置】ruoyi-framework / interceptor层 —— Spring MVC拦截器，在Controller方法执行前（preHandle）介入。
 * 【解决场景】用户快速双击"保存"按钮、网络重试、刷新重发等导致的表单重复提交，产生重复数据。
 * <p>
 * 【使用方式】Controller方法上标注 @RepeatSubmit(interval = 5000) → 本拦截器检查同用户同URL同参数在
 * 指定毫秒数内是否已提交过，是则直接返回错误JSON、不再进入Controller（return false 拦截请求）。
 * <p>
 * 【设计模式】模板方法模式：本类定义拦截流程，具体"如何判定重复"由子类 SameUrlDataInterceptor 实现。
 *
 * @author ruoyi
 */
@Component
public abstract class RepeatSubmitInterceptor implements HandlerInterceptor
{
    /**
     * Controller方法执行前的拦截入口
     *
     * @return true=放行进入Controller；false=拦截（已向前端写出重复提交的错误提示）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        // 只拦截Controller方法请求（静态资源等直接放行）
        if (handler instanceof HandlerMethod)
        {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            // 反射读取方法上的 @RepeatSubmit 注解，没有标注则不检查
            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
            if (annotation != null)
            {
                if (this.isRepeatSubmit(request, annotation))
                {
                    // 判定为重复提交：直接把错误JSON写入响应，前端收到 {"code":500,"msg":"不允许重复提交..."}
                    AjaxResult ajaxResult = AjaxResult.error(annotation.message());
                    ServletUtils.renderString(response, JSON.toJSONString(ajaxResult));
                    return false;
                }
            }
            return true;
        }
        else
        {
            return true;
        }
    }

    /**
     * 验证是否重复提交由子类实现具体的防重复提交的规则
     *
     * @param request 请求信息
     * @param annotation 防重复注解参数
     * @return 结果
     * @throws Exception
     */
    public abstract boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation);
}
