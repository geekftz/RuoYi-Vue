package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysLogininfor;

/**
 * 系统访问日志情况信息 数据层
 * <p>
 * 【架构位置】ruoyi-system → mapper，MyBatis 数据访问接口，对应数据库表 sys_logininfor（登录日志），
 * SQL 实现在 resources/mapper/system/SysLogininforMapper.xml。
 * 【负责的数据操作】登录日志的新增（异步落库）、条件分页查询、批量删除、清空（truncate）。
 * 【调用方】SysLogininforServiceImpl；日志写入由 AsyncManager 异步触发，不走请求主线程。
 * 
 * @author ruoyi
 */
public interface SysLogininforMapper
{
    /**
     * 新增系统登录日志
     * 
     * @param logininfor 访问日志对象
     */
    public void insertLogininfor(SysLogininfor logininfor);

    /**
     * 查询系统登录日志集合
     * 
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     * 批量删除系统登录日志
     * 
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    public int deleteLogininforByIds(Long[] infoIds);

    /**
     * 清空系统登录日志
     * 
     * @return 结果
     */
    public int cleanLogininfor();
}
