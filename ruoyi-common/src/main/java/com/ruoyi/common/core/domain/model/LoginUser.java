package com.ruoyi.common.core.domain.model;

import com.alibaba.fastjson2.annotation.JSONField;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Set;

/**
 * 登录用户身份权限
 * <p>
 * 【架构位置】common模块 → core/domain/model，贯穿全站的"登录态载体"。
 * 【核心职责】封装当前登录用户的全部身份信息：用户ID、部门ID、Token、登录时间、过期时间、
 *             登录IP/地点/浏览器/操作系统、权限集合、SysUser用户详情。
 * 【调用链路】登录成功后由TokenService.createToken()创建本对象 → 存入Redis（key=login_tokens:token）→
 *             之后每次请求JwtAuthenticationTokenFilter从Redis取出本对象 → 存入SecurityContextHolder →
 *             业务代码通过SecurityUtils.getLoginUser()随时取出使用。
 * 【为什么实现UserDetails】Spring Security的Authentication体系要求principal（当事人）是UserDetails类型，
 *             若依把LoginUser作为principal塞进UsernamePasswordAuthenticationToken，
 *             这样SecurityContextHolder里存的就是LoginUser，SecurityUtils才能直接强转取出。
 * 【前端联动】本对象的permissions就是前端v-hasPermi指令和路由守卫做按钮/菜单权限判断的数据来源
 *             （前端从getInfo接口拿到permissions数组）。
 * 
 * @author ruoyi
 */
public class LoginUser implements UserDetails
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户唯一标识
     * <p>
     * 【重点掌握】这个token不是JWT本身，而是UUID随机串，它是Redis中登录信息的key后缀：
     * 完整Redis key = "login_tokens:" + 本token值。
     * 前端请求头Authorization: Bearer xxx 中的xxx是JWT，JWT解开后里面的login_user_key才是本token。
     * 【双重设计原因】JWT负责"防篡改传输"，Redis负责"可主动失效"——改密码/踢人下线时直接删Redis即可让Token立即失效。
     */
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 权限列表
     * <p>
     * 【内容】形如 ["system:user:list","system:user:add",...] 的权限字符串集合，登录时由SysPermissionService查库组装。
     * 【使用场景】@PreAuthorize("@ss.hasPermi('system:user:list')") 校验时就是在这个Set里做contains匹配；
     * 超管admin的Set里只有一个"*:*:*"通配符，匹配一切。
     * 【前端联动】前端getInfo接口返回的permissions字段即此集合，用于按钮级权限控制v-hasPermi。
     */
    private Set<String> permissions;

    /**
     * 用户信息
     */
    private SysUser user;

    public LoginUser()
    {
    }

    public LoginUser(SysUser user, Set<String> permissions)
    {
        this.user = user;
        this.permissions = permissions;
    }

    public LoginUser(Long userId, Long deptId, SysUser user, Set<String> permissions)
    {
        this.userId = userId;
        this.deptId = deptId;
        this.user = user;
        this.permissions = permissions;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    /**
     * 获取用户密码（来自内部SysUser）
     * <p>
     * 【注解专项】@JSONField(serialize = false)：fastjson2序列化时跳过此字段。
     * 因为LoginUser整体要序列化存入Redis，密码（哪怕是BCrypt密文）也不该进缓存，防止泄露。
     * 【注意】这里用的是fastjson2的注解而非Jackson的，因为若依Redis序列化器用的是fastjson2。
     */
    @JSONField(serialize = false)
    @Override
    public String getPassword()
    {
        return user.getPassword();
    }

    @Override
    public String getUsername()
    {
        return user.getUserName();
    }

    /**
     * 账户是否未过期,过期无法验证
     * <p>
     * 【为何恒返回true】这是Spring Security的UserDetails接口约定方法，但若依不使用Spring Security自带的
     * 账号过期/锁定/禁用校验体系，而是在SysLoginService.login()里用自己的逻辑校验
     * （查sys_user表的status、del_flag字段，抛自定义异常）。所以这里四个状态方法全部返回true，
     * 相当于告诉Spring Security"这些检查我自己做过了，你别管"。
     * @JSONField(serialize = false)同理：这些Spring Security契约字段无需存入Redis。
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    /**
     * 指定用户是否解锁,锁定的用户无法进行身份验证
     * 
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    /**
     * 指示是否已过期的用户的凭据(密码),过期的凭据防止认证
     * 
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    /**
     * 是否可用 ,禁用的用户不能身份验证
     * 
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public boolean isEnabled()
    {
        return true;
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation()
    {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation)
    {
        this.loginLocation = loginLocation;
    }

    public String getBrowser()
    {
        return browser;
    }

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public SysUser getUser()
    {
        return user;
    }

    public void setUser(SysUser user)
    {
        this.user = user;
    }

    /**
     * 获取Spring Security标准的权限列表
     * <p>
     * 【为何返回null】Spring Security标准做法是把权限包装成GrantedAuthority对象集合返回，
     * 但若依没有使用这套体系，而是用自己设计的Set<String> permissions + @ss.hasPermi()做权限判断。
     * 此方法仅为满足UserDetails接口必须实现的要求，直接返回null即可，若依代码全程不调用它。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return null;
    }
}
