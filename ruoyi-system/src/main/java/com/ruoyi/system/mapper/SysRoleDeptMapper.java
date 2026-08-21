package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysRoleDept;

/**
 * 角色与部门关联表 数据层
 * <p>
 * 【架构位置】ruoyi-system → mapper，MyBatis 数据访问接口，对应数据库表 sys_role_dept（角色-部门关联，数据权限基础）。
 * 【负责的数据操作】关联记录的增删与统计：删除角色时清理关联、修改角色数据权限时先删后插（batchRoleDept）、
 * 删除部门前检查是否被角色引用（selectCountRoleDeptByDeptId）。
 * 【调用方】SysRoleServiceImpl / SysDeptServiceImpl。
 * 
 * @author ruoyi
 */
public interface SysRoleDeptMapper
{
    /**
     * 通过角色ID删除角色和部门关联
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleDeptByRoleId(Long roleId);

    /**
     * 批量删除角色部门关联信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteRoleDept(Long[] ids);

    /**
     * 查询部门使用数量
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    public int selectCountRoleDeptByDeptId(Long deptId);

    /**
     * 批量新增角色部门信息
     * 
     * @param roleDeptList 角色部门列表
     * @return 结果
     */
    public int batchRoleDept(List<SysRoleDept> roleDeptList);
}
