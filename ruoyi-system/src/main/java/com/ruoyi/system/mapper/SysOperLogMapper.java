package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysOperLog;

/**
 * 操作日志 数据层
 * <p>
 * 【架构位置】ruoyi-system → mapper，MyBatis 数据访问接口，对应数据库表 sys_oper_log（操作日志）。
 * 【负责的数据操作】操作日志的新增（由 LogAspect 切面异步触发）、条件分页查询（支持 businessTypes 多类型筛选）、
 * 详情查询、批量删除、清空。
 * 【调用方】SysOperLogServiceImpl ← SysOperLogController / LogAspect。
 * 
 * @author ruoyi
 */
public interface SysOperLogMapper
{
    /**
     * 新增操作日志
     * 
     * @param operLog 操作日志对象
     */
    public void insertOperlog(SysOperLog operLog);

    /**
     * 查询系统操作日志集合
     * 
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    public List<SysOperLog> selectOperLogList(SysOperLog operLog);

    /**
     * 批量删除系统操作日志
     * 
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    public int deleteOperLogByIds(Long[] operIds);

    /**
     * 查询操作日志详细
     * 
     * @param operId 操作ID
     * @return 操作日志对象
     */
    public SysOperLog selectOperLogById(Long operId);

    /**
     * 清空操作日志
     */
    public void cleanOperLog();
}
