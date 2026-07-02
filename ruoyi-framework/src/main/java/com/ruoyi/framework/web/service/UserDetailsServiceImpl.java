package com.ruoyi.framework.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;

/**
 * Spring Security 用户详情服务实现
 * <p>
 * 实现 UserDetailsService 接口，是 Spring Security 认证流程的核心组件。
 * 当 AuthenticationManager.authenticate() 被调用时，Spring Security 会自动调用此类的
 * loadUserByUsername 方法来加载用户信息。
 * </p>
 * <p>
 * 认证流程：
 * 1. 根据用户名从数据库查询用户信息（含角色、部门）
 * 2. 校验用户是否存在、是否已删除、是否已停用
 * 3. 调用 SysPasswordService.validate 校验密码并检查错误次数限制
 * 4. 构建LoginUser对象（含用户信息和菜单权限）返回给Spring Security
 * </p>
 *
 * @author ruoyi
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    /** 用户业务层 */
    @Autowired
    private ISysUserService userService;

    /** 密码校验服务 */
    @Autowired
    private SysPasswordService passwordService;

    /** 权限服务 */
    @Autowired
    private SysPermissionService permissionService;

    /**
     * 根据用户名加载用户详情（Spring Security 认证入口）
     * <p>
     * 此方法由 AuthenticationManager.authenticate() 内部调用，是认证流程的核心环节。
     * 业务流程：
     * 1. 从数据库查询用户信息（联查角色、部门）
     * 2. 用户不存在 → 抛出 ServiceException
     * 3. 用户已被逻辑删除 → 抛出 ServiceException
     * 4. 用户已被停用 → 抛出 ServiceException
     * 5. 调用密码服务校验密码正确性及错误次数限制
     * 6. 构建LoginUser对象（含用户信息和菜单权限标识集合）
     * </p>
     *
     * @param username 用户名
     * @return Spring Security 的 UserDetails 对象（实际为 LoginUser）
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        // 从数据库查询用户信息（包含角色和部门联查）
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user))
        {
            // 用户不存在
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            // 用户已被逻辑删除（del_flag=2）
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            // 用户已被管理员停用（status=1）
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }

        // 校验密码：检查错误次数限制 + 密码是否匹配
        passwordService.validate(user);

        // 构建并返回LoginUser对象（包含用户信息和菜单权限标识）
        return createLoginUser(user);
    }

    /**
     * 构建登录用户信息
     *
     * @param user 系统用户
     * @return 登录用户对象
     */
    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
