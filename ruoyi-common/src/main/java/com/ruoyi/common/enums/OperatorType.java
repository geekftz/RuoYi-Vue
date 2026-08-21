package com.ruoyi.common.enums;

/**
 * 操作人类别
 * <p>
 * 【使用者】@Log注解的operatorType属性，标识操作来自后台管理端还是手机端，写入sys_oper_log.operator_type列。
 * 默认MANAGE（后台用户）；若项目扩展了移动端接口，在移动端Controller的@Log上标MOBILE区分日志来源。
 * 
 * @author ruoyi
 */
public enum OperatorType
{
    /**
     * 其它
     */
    OTHER,

    /**
     * 后台用户
     */
    MANAGE,

    /**
     * 手机端用户
     */
    MOBILE
}
