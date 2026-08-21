package com.ruoyi.common.enums;

/**
 * 业务操作类型
 * <p>
 * 【使用者】@Log注解的businessType属性取值，标识这次操作是增/删/改/导出等，
 * LogAspect写入sys_oper_log.business_type列。前端操作日志页据此显示操作类型标签。
 * 【扩展】若业务有特殊操作类型（如"审核"），在此追加枚举值即可，无需改其他代码。
 * 
 * @author ruoyi
 */
public enum BusinessType
{
    /**
     * 其它
     */
    OTHER,

    /**
     * 新增
     */
    INSERT,

    /**
     * 修改
     */
    UPDATE,

    /**
     * 删除
     */
    DELETE,

    /**
     * 授权
     */
    GRANT,

    /**
     * 导出
     */
    EXPORT,

    /**
     * 导入
     */
    IMPORT,

    /**
     * 强退
     */
    FORCE,

    /**
     * 生成代码
     */
    GENCODE,
    
    /**
     * 清空数据
     */
    CLEAN,
}
