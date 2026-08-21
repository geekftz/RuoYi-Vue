package com.ruoyi.common.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.PatternMatchUtils;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

/**
 * 安全服务工具类（若依核心工具 · 重点掌握）
 * <p>
 * 【架构位置】ruoyi-common / utils层 —— 业务代码获取"当前登录用户"的唯一入口。
 * <p>
 * 【完整链路：登录用户信息从哪来】
 * ① 前端请求头携带 Authorization: Bearer {token}；
 * ② JwtAuthenticationTokenFilter 解析token，从Redis取出LoginUser，
 *    包装成 UsernamePasswordAuthenticationToken 存入 SecurityContextHolder（绑定当前线程）；
 * ③ Controller/Service中调用 SecurityUtils.getLoginUser()/getUserId()/getUsername()，
 *    本质就是从 SecurityContextHolder 里取出第②步放入的对象。
 * <p>
 * 【常用示例】
 *   SecurityUtils.getUserId()    —— 新增数据时填充 create_by；
 *   SecurityUtils.getUsername()  —— 记录操作人；
 *   SecurityUtils.encryptPassword(pwd) —— 新增/重置用户密码时加密入库；
 *   SecurityUtils.getLoginUser().getDeptId() —— 数据权限相关逻辑。
 * <p>
 * 【注意】未登录环境（如定时任务线程）调用会抛 ServiceException(401)，因为ThreadLocal里没有认证信息。
 *
 * @author ruoyi
 */
public class SecurityUtils
{

    /**
     * 用户ID
     * <p>
     * 取当前登录用户的 user_id（sys_user表主键）；新增/修改记录时用于填充 createBy/updateBy
     **/
    public static Long getUserId()
    {
        try
        {
            return getLoginUser().getUserId();
        }
        catch (Exception e)
        {
            // 未登录/Token失效时走到这里：统一转为401业务异常，全局异常处理器返回 {"code":401,...}
            throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取部门ID
     **/
    public static Long getDeptId()
    {
        try
        {
            return getLoginUser().getDeptId();
        }
        catch (Exception e)
        {
            throw new ServiceException("获取部门ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户账户
     **/
    public static String getUsername()
    {
        try
        {
            return getLoginUser().getUsername();
        }
        catch (Exception e)
        {
            throw new ServiceException("获取用户账户异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户
     * <p>
     * 【核心原理】SecurityContextHolder.getContext().getAuthentication() 拿到当前线程的认证对象，
     * 其 principal（当事人）就是 JwtAuthenticationTokenFilter 放入的 LoginUser。
     * LoginUser内含：SysUser用户实体、角色Set、权限字符串Set、Token过期时间等
     **/
    public static LoginUser getLoginUser()
    {
        try
        {
            return (LoginUser) getAuthentication().getPrincipal();
        }
        catch (Exception e)
        {
            throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取Authentication（Spring Security认证对象，包含principal=LoginUser、credentials、authorities）
     */
    public static Authentication getAuthentication()
    {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     * <p>
     * 新增用户/重置密码时调用，返回值形如 $2a$10$...（含随机盐），直接存入 sys_user.password
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password)
    {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     * <p>
     * 登录/修改密码时比对：BCrypt每次加密结果不同，不能重新加密比对，必须用 matches() 内部解析盐值再验算
     *
     * @param rawPassword 真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword)
    {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否为管理员
     * 
     * @return 结果
     */
    public static boolean isAdmin()
    {
        return isAdmin(getUserId());
    }

    /**
     * 是否为管理员
     * <p>
     * 若依约定：user_id = 1 的账号为超级管理员，拥有最高权限（不走数据库角色/权限校验）
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId)
    {
        return userId != null && 1L == userId;
    }

    /**
     * 验证用户是否具备某权限
     * 
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public static boolean hasPermi(String permission)
    {
        return hasPermi(getLoginUser().getPermissions(), permission);
    }

    /**
     * 判断是否包含权限
     * <p>
     * 支持通配匹配：PatternMatchUtils.simpleMatch 支持 * 通配符（如 system:user:* 可匹配 system:user:list）；
     * 用户集合中含 *:*:*（Constants.ALL_PERMISSION，超管专属）直接放行
     * 
     * @param authorities 权限列表
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public static boolean hasPermi(Collection<String> authorities, String permission)
    {
        return authorities.stream().filter(StringUtils::hasText)
                .anyMatch(x -> Constants.ALL_PERMISSION.equals(x) || PatternMatchUtils.simpleMatch(x, permission));
    }

    /**
     * 验证用户是否拥有某个角色
     * 
     * @param role 角色标识
     * @return 用户是否具备某角色
     */
    public static boolean hasRole(String role)
    {
        List<SysRole> roleList = getLoginUser().getUser().getRoles();
        Collection<String> roles = roleList.stream().map(SysRole::getRoleKey).collect(Collectors.toSet());
        return hasRole(roles, role);
    }

    /**
     * 判断是否包含角色
     * 
     * @param roles 角色列表
     * @param role 角色
     * @return 用户是否具备某角色权限
     */
    public static boolean hasRole(Collection<String> roles, String role)
    {
        return roles.stream().filter(StringUtils::hasText)
                .anyMatch(x -> Constants.SUPER_ADMIN.equals(x) || PatternMatchUtils.simpleMatch(x, role));
    }

}
