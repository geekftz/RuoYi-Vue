package com.ruoyi.framework.web.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.UserAgentUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.servlet.http.HttpServletRequest;

/**
 * Token 令牌验证处理服务
 * <p>
 * 核心职责：
 * 1. 创建JWT令牌 —— 用户登录成功后生成包含UUID的JWT，UUID作为Redis缓存key的一部分
 * 2. 解析JWT令牌 —— 从请求头中提取令牌，解析出UUID，再从Redis中获取完整的LoginUser
 * 3. 令牌自动续期 —— 当令牌剩余有效期不足20分钟时，自动刷新Redis中的缓存过期时间
 * 4. 登录/登出管理 —— 将用户身份信息存入或移出Redis缓存
 * </p>
 * <p>
 * 认证流程：客户端每次请求携带JWT → JwtAuthenticationTokenFilter解析令牌 →
 * 从Redis获取LoginUser → 注入Spring Security上下文 → 后续接口通过SecurityUtils获取当前用户
 * </p>
 *
 * @author ruoyi
 */
@Component
public class TokenService
{
    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    /** 令牌自定义标识 */
    @Value("${token.header}")
    private String header;

    /** 令牌秘钥 */
    @Value("${token.secret}")
    private String secret;

    /** 令牌有效期（默认30分钟） */
    @Value("${token.expireTime}")
    private int expireTime;

    /** 毫秒常量 */
    protected static final long MILLIS_SECOND = 1000;

    /** 分钟毫秒常量 */
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    /** 令牌自动续期阈值（20分钟） */
    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    /** Redis 缓存 */
    @Autowired
    private RedisCache redisCache;

    /**
     * 从HTTP请求中获取用户身份信息
     * <p>
     * 业务流程：
     * 1. 从请求头中提取JWT令牌（去掉"Bearer "前缀）
     * 2. 用密钥解析JWT，取出其中存储的UUID
     * 3. 用UUID拼接Redis缓存key，从Redis中反序列化出LoginUser对象
     * </p>
     *
     * @param request HTTP请求对象
     * @return 用户身份信息，令牌无效或Redis中不存在时返回null
     */
    public LoginUser getLoginUser(HttpServletRequest request)
    {
        // 从请求头中提取并去除Bearer前缀后的纯JWT字符串
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                // 使用密钥解析JWT，获取Claims（令牌声明信息）
                Claims claims = parseToken(token);
                // 从Claims中取出登录时生成的UUID（作为Redis缓存的唯一标识）
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                // 拼接Redis缓存key：login_tokens:{uuid}
                String userKey = getTokenKey(uuid);
                // 从Redis中获取完整的登录用户信息
                LoginUser user = redisCache.getCacheObject(userKey);
                return user;
            }
            catch (Exception e)
            {
                // 令牌过期或篡改时，解析会抛异常，记录日志但不中断请求
                log.error("获取用户信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 设置（更新）用户身份信息到缓存
     * <p>
     * 当用户权限或信息发生变更时，调用此方法刷新Redis中的缓存。
     * 实际委托给refreshToken完成缓存更新和过期时间重置。
     * </p>
     *
     * @param loginUser 登录用户信息
     */
    public void setLoginUser(LoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            // 刷新令牌：更新登录时间和过期时间，并重新写入Redis
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户身份信息（用户登出时调用）
     * <p>
     * 根据令牌UUID拼接Redis key并删除缓存，使用户后续请求无法通过认证。
     * </p>
     *
     * @param token 用户令牌UUID
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 创建JWT令牌（用户登录成功后调用）
     * <p>
     * 业务流程：
     * 1. 生成一个快速UUID作为令牌标识，存入LoginUser
     * 2. 解析请求User-Agent，填充用户的IP、登录地点、浏览器、操作系统信息
     * 3. 调用refreshToken将LoginUser写入Redis（设置过期时间）
     * 4. 将UUID和用户名放入JWT的Claims，用HS512算法签名生成最终JWT字符串
     * </p>
     * <p>
     * 设计说明：JWT本身不存储用户信息，只存储UUID；完整的用户信息存在Redis中。
     * 这样既保证了令牌的轻量性，又能通过Redis主动控制会话过期和踢出。
     * </p>
     *
     * @param loginUser 登录用户信息
     * @return 签名后的JWT令牌字符串
     */
    public String createToken(LoginUser loginUser)
    {
        // 生成UUID作为Redis缓存的唯一标识
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        // 解析User-Agent，填充客户端信息（IP、地点、浏览器、操作系统）
        setUserAgent(loginUser);
        // 将LoginUser写入Redis，设置过期时间
        refreshToken(loginUser);

        // 构建JWT声明信息
        Map<String, Object> claims = new HashMap<>();
        // 存入UUID，后续解析令牌时用此UUID从Redis获取完整用户信息
        claims.put(Constants.LOGIN_USER_KEY, token);
        // 存入用户名（仅用于日志展示，不做认证依据）
        claims.put(Constants.JWT_USERNAME, loginUser.getUsername());
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，不足20分钟时自动续期
     * <p>
     * 在JwtAuthenticationTokenFilter每次请求时调用，实现"滑动过期"效果：
     * 只要用户持续活跃，令牌就不会过期；只有超过20分钟无请求才会真正过期。
     * </p>
     *
     * @param loginUser 登录用户信息
     */
    public void verifyToken(LoginUser loginUser)
    {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        // 距过期不足20分钟时，刷新Redis缓存中的过期时间
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期（核心续期方法）
     * <p>
     * 1. 更新LoginUser的登录时间为当前时间
     * 2. 计算新的过期时间 = 当前时间 + 配置的有效期（默认30分钟）
     * 3. 将更新后的LoginUser重新写入Redis，并重置过期时间
     * </p>
     *
     * @param loginUser 登录用户信息
     */
    public void refreshToken(LoginUser loginUser)
    {
        // 更新登录时间为当前时间戳
        loginUser.setLoginTime(System.currentTimeMillis());
        // 计算过期时间 = 登录时间 + 有效期（分钟转毫秒）
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据UUID拼接Redis key，将完整的LoginUser序列化存入Redis
        String userKey = getTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 解析并设置客户端User-Agent信息
     * <p>
     * 从HTTP请求头中解析出客户端的IP地址、登录地点、浏览器类型和操作系统，
     * 用于登录日志记录和安全审计。
     * </p>
     *
     * @param loginUser 登录用户信息
     */
    public void setUserAgent(LoginUser loginUser)
    {
        // 获取请求头中的User-Agent字符串
        String userAgent = ServletUtils.getRequest().getHeader("User-Agent");
        // 获取客户端IP地址（支持代理穿透）
        String ip = IpUtils.getIpAddr();
        loginUser.setIpaddr(ip);
        // 根据IP查询归属地
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        // 解析浏览器类型
        loginUser.setBrowser(UserAgentUtils.getBrowser(userAgent));
        // 解析操作系统
        loginUser.setOs(UserAgentUtils.getOperatingSystem(userAgent));
    }

    /**
     * 从数据声明生成JWT令牌（底层方法）
     * <p>
     * 使用HS512签名算法对Claims进行签名，生成最终的JWT字符串。
     * HS512是对称加密，secret密钥配置在application.yml中。
     * </p>
     *
     * @param claims JWT数据声明（包含UUID和用户名）
     * @return 签名后的JWT令牌字符串
     */
    private String createToken(Map<String, Object> claims)
    {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 从JWT令牌中解析出数据声明
     * <p>
     * 使用相同的secret密钥验证签名并解析JWT。如果令牌被篡改或已过期，会抛出异常。
     * </p>
     *
     * @param token JWT令牌字符串
     * @return JWT数据声明（Claims）
     */
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从JWT令牌中获取用户名
     *
     * @param token JWT令牌字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token)
    {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从HTTP请求头中获取令牌字符串
     * <p>
     * 请求头格式为 "Authorization: Bearer xxx.yyy.zzz"，
     * 此方法去除"Bearer "前缀，返回纯JWT字符串。
     * </p>
     *
     * @param request HTTP请求对象
     * @return 纯JWT令牌字符串，请求头不存在时返回null
     */
    private String getToken(HttpServletRequest request)
    {
        // 从请求头中获取header字段（默认为Authorization）的值
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            // 去除"Bearer "前缀
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 获取令牌缓存键名
     *
     * @param uuid 用户令牌UUID
     * @return Redis缓存键名
     */
    private String getTokenKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }

    /**
     * 角色权限变更后，刷新所有持有该角色的在线用户权限
     * <p>
     * 当管理员修改了某个角色的菜单权限后，需要实时生效到该角色下所有在线用户。
     * 业务流程：
     * 1. 通过通配符扫描Redis中所有在线用户的缓存key
     * 2. 逐个遍历，跳过超级管理员（admin拥有全部权限，无需刷新）
     * 3. 判断用户是否拥有变更的角色，不匹配则跳过
     * 4. 对匹配的用户重新获取最新权限并刷新Redis缓存
     * </p>
     *
     * @param roleId            变更的角色ID
     * @param permissionService 权限服务
     */
    public void refreshPermissionByRoleId(Long roleId, SysPermissionService permissionService)
    {
        // 扫描Redis中所有以login_tokens:开头的key，即所有在线用户的缓存
        String pattern = CacheConstants.LOGIN_TOKEN_KEY + "*";
        Collection<String> keys = redisCache.keys(pattern);
        if (keys == null || keys.isEmpty())
        {
            return;
        }
        for (String key : keys)
        {
            LoginUser loginUser = redisCache.getCacheObject(key);
            if (loginUser == null || loginUser.getUser() == null || loginUser.getUser().isAdmin())
            {
                // 超级管理员拥有所有权限，无需刷新
                continue;
            }
            // 判断该用户的角色列表中是否包含变更的角色
            boolean hasRole = loginUser.getUser().getRoles() != null
                    && loginUser.getUser().getRoles().stream().anyMatch(r -> roleId.equals(r.getRoleId()));
            if (!hasRole)
            {
                // 用户不持有该角色，无需刷新
                continue;
            }
            // 重新获取该用户的菜单权限（基于最新的角色-菜单关系）
            loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
            // 刷新Redis缓存，使新权限立即生效
            refreshToken(loginUser);
            log.info("角色[{}]权限变更，已刷新在线用户[{}]的权限缓存", roleId, loginUser.getUsername());
        }
    }
}
