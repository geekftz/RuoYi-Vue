package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysUserRole;

/**
 * 用户与角色关联表 数据层
 * <p>
 * 【架构位置】ruoyi-system → mapper，MyBatis 数据访问接口，对应数据库表 sys_user_role（用户-角色关联）。
 * 【负责的数据操作】关联记录的增删与统计：新增/编辑用户时先删后插（batchUserRole）、删除角色前检查占用、
 * 角色详情页「分配用户」的选择授权/批量取消授权（deleteUserRoleInfo/deleteUserRoleInfos）。
 * 【注解专项】@Param：多参数方法必须用它命名参数，XML 中才能用 #{roleId} 按名取值（否则只能用 arg0/param1）。
 * 【调用方】SysUserServiceImpl / SysRoleServiceImpl。
 * 
 * @author ruoyi
 */
public interface SysUserRoleMapper
{
    /**
     * 通过用户ID删除用户和角色关联
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteUserRoleByUserId(Long userId);

    /**
     * 批量删除用户和角色关联
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteUserRole(Long[] ids);

    /**
     * 通过角色ID查询角色使用数量
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int countUserRoleByRoleId(Long roleId);

    /**
     * 批量新增用户角色信息
     * 
     * @param userRoleList 用户角色列表
     * @return 结果
     */
    public int batchUserRole(List<SysUserRole> userRoleList);

    /**
     * 删除用户和角色关联信息
     * 
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    public int deleteUserRoleInfo(SysUserRole userRole);

    /**
     * 批量取消授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    public int deleteUserRoleInfos(@Param("roleId") Long roleId, @Param("userIds") Long[] userIds);
}
