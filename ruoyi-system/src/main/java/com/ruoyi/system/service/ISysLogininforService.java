package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysLogininfor;

/**
 * 系统访问日志情况信息 服务层
 * <p>
 * 【架构位置】ruoyi-system → service，登录日志业务接口，实现类 SysLogininforServiceImpl。
 * 【业务职责】登录日志的写入（SysLoginService 登录成败时异步调用）、分页查询、删除、清空、按IP/账号解锁。
 * 【调用方】SysLogininforController（「系统监控-登录日志」）/ SysLoginService / SysPasswordService。
 * 
 * @author ruoyi
 */
public interface ISysLogininforService
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
     */
    public void cleanLogininfor();
}
