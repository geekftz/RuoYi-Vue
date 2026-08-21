package com.ruoyi.common.exception.job;

/**
 * 计划策略异常
 * <p>
 * 【架构位置】exception/job，定时任务模块（ruoyi-quartz）专用异常。
 * 【触发场景】Quartz定时任务操作失败时抛出：如新增的任务已存在、启动不存在的任务、cron表达式配置错误等，
 * 由SysJobServiceImpl中的任务操作方法抛出，全局异常处理器捕获后返回前端。
 * 【设计说明】继承Exception（受检异常），强制调用方try-catch处理，因为任务操作属于重要管理动作；
 * 内置Code枚举明确错误类型，比单纯靠message字符串判断更可靠。
 * 
 * @author ruoyi
 */
public class TaskException extends Exception
{
    private static final long serialVersionUID = 1L;

    /** 错误类型编码，见下方Code枚举 */
    private Code code;

    /**
     * @param msg 错误描述信息
     * @param code 错误类型（如TASK_EXISTS表示任务已存在）
     */
    public TaskException(String msg, Code code)
    {
        this(msg, code, null);
    }

    /**
     * @param msg 错误描述信息
     * @param code 错误类型
     * @param nestedEx 底层原始异常（如Quartz框架抛出的SchedulerException）
     */
    public TaskException(String msg, Code code, Exception nestedEx)
    {
        super(msg, nestedEx);
        this.code = code;
    }

    public Code getCode()
    {
        return code;
    }

    /**
     * 任务异常类型枚举：
     * TASK_EXISTS-任务已存在; NO_TASK_EXISTS-任务不存在; TASK_ALREADY_STARTED-任务已在运行;
     * UNKNOWN-未知错误; CONFIG_ERROR-配置错误（如cron表达式非法）; TASK_NODE_NOT_AVAILABLE-任务节点不可用
     */
    public enum Code
    {
        TASK_EXISTS, NO_TASK_EXISTS, TASK_ALREADY_STARTED, UNKNOWN, CONFIG_ERROR, TASK_NODE_NOT_AVAILABLE
    }
}