package com.ruoyi.framework.security.context;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import com.ruoyi.common.core.text.Convert;

/**
 * 权限信息
 * <p>
 * 【架构位置】ruoyi-framework / security / context层 —— 当前请求"正在校验哪个权限"的暂存器。
 * 【作用】@PreAuthorize("@ss.hasPermi('system:user:list')") 触发校验时，PermissionService 先把
 * 权限字符串存进本类；后续若发生权限不足异常，全局异常处理器能从这里取出"缺少哪个权限"返回给前端。
 * <p>
 * 【存储介质】存在 RequestAttributes（SCOPE_REQUEST 请求作用域），生命周期=一次HTTP请求，请求结束自动销毁。
 *
 * @author ruoyi
 */
public class PermissionContextHolder
{
    /** 权限字符串在请求属性中的键名 */
    private static final String PERMISSION_CONTEXT_ATTRIBUTES = "PERMISSION_CONTEXT";

    /**
     * 把正在校验的权限字符串存入当前请求属性
     *
     * @param permission 权限标识，如 system:user:list
     */
    public static void setContext(String permission)
    {
        RequestContextHolder.currentRequestAttributes().setAttribute(PERMISSION_CONTEXT_ATTRIBUTES, permission,
                RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 从当前请求属性中取出正在校验的权限字符串（无则返回空串）
     */
    public static String getContext()
    {
        return Convert.toStr(RequestContextHolder.currentRequestAttributes().getAttribute(PERMISSION_CONTEXT_ATTRIBUTES,
                RequestAttributes.SCOPE_REQUEST));
    }
}
