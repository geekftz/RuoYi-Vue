package com.ruoyi.common.exception;

/**
 * 工具类异常
 * <p>
 * 【定位】工具类内部把受检异常（IOException、SQLException等）包装成非受检异常抛出，
 * 让调用工具方法的业务代码不用强制try-catch或throws。
 * 【使用示例】DateUtils.parseDate()内部parse失败时抛UtilException，调用方不处理则由全局异常处理器兜底。
 * 
 * @author ruoyi
 */
public class UtilException extends RuntimeException
{
    private static final long serialVersionUID = 8247610319171014183L;

    public UtilException(Throwable e)
    {
        super(e.getMessage(), e);
    }

    public UtilException(String message)
    {
        super(message);
    }

    public UtilException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
