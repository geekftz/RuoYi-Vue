package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 角色和部门关联 sys_role_dept
 * <p>
 * 【架构位置】ruoyi-system → domain，对应数据库中间表 sys_role_dept（多对多关联表）。
 * 【业务作用】实现「数据权限」：给角色绑定一批部门后，拥有该角色的用户查询业务列表时，
 * DataScopeAspect 会拼 SQL 只查出这些部门的数据（角色→部门 的可见范围）。
 * 【典型场景】角色管理页面「数据权限」标签页勾选部门树时，后端批量写入此表。
 * 【注意】纯关联表，只有两列外键，无主键无审计字段，所以不继承 BaseEntity。
 * 
 * @author ruoyi
 */
public class SysRoleDept
{
    /** 角色ID：外键关联 sys_role.role_id */
    private Long roleId;
    
    /** 部门ID：外键关联 sys_dept.dept_id，表示该角色能看到这个部门的数据 */
    private Long deptId;

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("roleId", getRoleId())
            .append("deptId", getDeptId())
            .toString();
    }
}
