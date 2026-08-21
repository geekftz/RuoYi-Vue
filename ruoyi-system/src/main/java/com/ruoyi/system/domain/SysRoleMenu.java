package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 角色和菜单关联 sys_role_menu
 * <p>
 * 【架构位置】ruoyi-system → domain，对应数据库中间表 sys_role_menu（角色-菜单多对多关联）。
 * 【业务作用】RBAC 权限模型的核心纽带之二：角色绑菜单（含按钮级权限标识 perms），
 * 用户登录后按「用户→角色→菜单」汇总出可见菜单树与 @PreAuthorize 校验用的权限串集合。
 * 【典型场景】角色管理页面「菜单权限」勾选树保存时，先删后插本表。
 * 【注意】纯关联表，只有两列外键，无主键无审计字段，所以不继承 BaseEntity。
 * 
 * @author ruoyi
 */
public class SysRoleMenu
{
    /** 角色ID：外键关联 sys_role.role_id */
    private Long roleId;
    
    /** 菜单ID：外键关联 sys_menu.menu_id（目录/菜单/按钮都算菜单） */
    private Long menuId;

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getMenuId()
    {
        return menuId;
    }

    public void setMenuId(Long menuId)
    {
        this.menuId = menuId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("roleId", getRoleId())
            .append("menuId", getMenuId())
            .toString();
    }
}
