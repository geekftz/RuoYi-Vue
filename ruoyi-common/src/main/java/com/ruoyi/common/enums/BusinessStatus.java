package com.ruoyi.common.enums;

/**
 * 操作状态
 * <p>
 * 【使用者】操作日志（sys_oper_log.status）与登录日志（sys_logininfor.status）的成败标识，
 * LogAspect记录日志时按方法是否抛异常设置SUCCESS/FAIL。前端日志列表用dict-tag渲染对应颜色。
 * 
 * @author ruoyi
 *
 */
public enum BusinessStatus
{
    /**
     * 成功
     */
    SUCCESS,

    /**
     * 失败
     */
    FAIL,
}
