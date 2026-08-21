package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户和角色关联 sys_user_role
 * <p>
 * 【架构位置】ruoyi-system → domain，对应数据库中间表 sys_user_role（用户-角色多对多关联）。
 * 【业务作用】RBAC 权限模型的核心纽带：用户不直接挂权限，而是通过「用户→角色→菜单(权限标识)」间接获得权限。
 * 登录时 SysLoginService 正是通过此表查出用户所有角色，再汇总出 permissions 集合塞进 LoginUser。
 * 【典型场景】用户管理页面给用户分配角色、角色管理页面批量授权用户时写此表。
 * 【注意】纯关联表，只有两列外键，无主键无审计字段，所以不继承 BaseEntity。
 * 
 * @author ruoyi
 */
public class SysUserRole
{
    /** 用户ID：外键关联 sys_user.user_id */
    private Long userId;
    
    /** 角色ID：外键关联 sys_role.role_id */
    private Long roleId;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("roleId", getRoleId())
            .toString();
    }
}
