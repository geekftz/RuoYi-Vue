package com.ruoyi.common.exception;

/**
 * 业务异常
 * <p>
 * 【架构位置】common模块 → exception，全系统使用频率最高的异常类，业务校验失败的统一出口。
 * 【使用场景】Service层校验不通过时主动抛出：throw new ServiceException("用户名已存在")；
 *             也可带错误码：throw new ServiceException("不允许操作", 500)。
 * 【处理链路】抛出后由全局异常处理器GlobalExceptionHandler（@RestControllerAdvice）捕获，
 *             加工成AjaxResult.error(message)返回前端：{"code":500,"msg":"用户名已存在"}，前端弹红色提示。
 * 【为何继承RuntimeException】非受检异常，Service方法签名无需throws声明；
 *             且Spring的@Transactional默认只对RuntimeException回滚，用它天然触发事务回滚。
 * 【为何final】禁止再被继承，业务异常统一用它，避免异常体系膨胀（子类化需求用BaseException体系）。
 * 
 * @author ruoyi
 */
public final class ServiceException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 错误码（可选，不传则前端按默认500处理；传601则前端弹黄色警告而非红色错误）
     */
    private Integer code;

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
    public ServiceException()
    {
    }

    public ServiceException(String message)
    {
        this.message = message;
    }

    public ServiceException(String message, Integer code)
    {
        this.message = message;
        this.code = code;
    }

    public String getDetailMessage()
    {
        return detailMessage;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public Integer getCode()
    {
        return code;
    }

    public ServiceException setMessage(String message)
    {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage)
    {
        this.detailMessage = detailMessage;
        return this;
    }
}