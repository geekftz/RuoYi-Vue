package com.ruoyi.framework.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysRoleService;

/**
 * 用户权限处理
 * <p>
 * 【架构位置】ruoyi-framework / web / service层 —— 登录用户"角色集合 + 权限字符串集合"的装配器。
 * 【调用链路】登录时 UserDetailsServiceImpl 查库组装 LoginUser 之前，调用本类：
 * getRolePermission() 收集角色标识（如 admin、common）→ 供前端路由和角色校验；
 * getMenuPermission() 收集菜单权限字符串（如 system:user:list）→ 供 @PreAuthorize("@ss.hasPermi()") 接口级校验。
 * <p>
 * 【超管特权】user.isAdmin()（userId=1）直接赋予 *:*:* 全部权限和 admin 角色，不查库。
 *
 * @author ruoyi
 */
@Component
public class SysPermissionService
{
    /** 角色服务：查用户的角色列表 */
    @Autowired
    private ISysRoleService roleService;

    /** 菜单服务：菜单表里存着每个菜单/按钮对应的权限字符串 */
    @Autowired
    private ISysMenuService menuService;

    /**
     * 获取角色数据权限
     * <p>
     * 返回该用户拥有的角色权限标识集合（roleKey），前端存于 store.user.roles，控制路由与按钮展示
     * 
     * @param user 用户信息
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(SysUser user)
    {
        Set<String> roles = new HashSet<String>();
        // 管理员拥有所有权限
        // 超管直接给 admin 角色标识（Constants.SUPER_ADMIN = "admin"），无需查库
        if (user.isAdmin())
        {
            roles.add(Constants.SUPER_ADMIN);
        }
        else
        {
            // 查库：sys_user → sys_user_role → sys_role，汇总所有启用角色的roleKey
            roles.addAll(roleService.selectRolePermissionByUserId(user.getUserId()));
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     * <p>
     * 返回权限字符串集合（perms），存进 LoginUser.permissions 并缓存到Redis；
     * 之后每次接口访问 @PreAuthorize("@ss.hasPermi('xxx')") 就拿 xxx 与此集合比对
     * 
     * @param user 用户信息
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(SysUser user)
    {
        Set<String> perms = new HashSet<String>();
        // 管理员拥有所有权限
        // 超管直接给通配权限 *:*:*（Constants.ALL_PERMISSION），hasPermi校验时直接通过
        if (user.isAdmin())
        {
            perms.add(Constants.ALL_PERMISSION);
        }
        else
        {
            List<SysRole> roles = user.getRoles();
            if (!CollectionUtils.isEmpty(roles))
            {
                // 多角色设置permissions属性，以便数据权限匹配权限
                // 按角色逐个查菜单权限并集；同时把权限集挂到role对象上，供数据权限切面（DataScopeAspect）使用
                for (SysRole role : roles)
                {
                    if (StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL) && !role.isAdmin())
                    {
                        Set<String> rolePerms = menuService.selectMenuPermsByRoleId(role.getRoleId());
                        role.setPermissions(rolePerms);
                        perms.addAll(rolePerms);
                    }
                }
            }
            else
            {
                // 没有角色的用户：按用户直连菜单查权限（sys_user → sys_user_role → sys_role_menu → sys_menu）
                perms.addAll(menuService.selectMenuPermsByUserId(user.getUserId()));
            }
        }
        return perms;
    }
}
