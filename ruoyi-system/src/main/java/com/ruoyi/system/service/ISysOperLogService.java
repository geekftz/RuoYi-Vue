package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysOperLog;

/**
 * 操作日志 服务层
 * <p>
 * 【架构位置】ruoyi-system → service，操作日志业务接口，实现类 SysOperLogServiceImpl。
 * 【业务职责】操作日志的写入（LogAspect 切面异步调用 insertOperlog）、分页查询、详情、删除、清空。
 * 【调用方】SysOperLogController（「系统监控-操作日志」）/ LogAspect（@Log 注解的切面）。
 * 
 * @author ruoyi
 */
public interface ISysOperLogService
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
