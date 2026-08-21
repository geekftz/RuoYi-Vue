package com.ruoyi.common.exception;

/**
 * 全局异常
 * <p>
 * 【定位】通用兜底异常，与ServiceException职责相近（历史遗留，两者并存）。
 * 【实际使用】项目中业务代码主要抛ServiceException；GlobalException偶见于需要区分"系统级"与"业务级"的场景。
 * 【处理链路】同样被GlobalExceptionHandler捕获，加工成AjaxResult返回前端。
 * 
 * @author ruoyi
 */
public class GlobalException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     *
     * 和 {@link CommonResult#getDetailMessage()} 一致的设计
     */
    private String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public GlobalException()
    {
    }

    public GlobalException(String message)
    {
        this.message = message;
    }

    public String getDetailMessage()
    {
        return detailMessage;
    }

    public GlobalException setDetailMessage(String detailMessage)
    {
        this.detailMessage = detailMessage;
        return this;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public GlobalException setMessage(String message)
    {
        this.message = message;
        return this;
    }
}