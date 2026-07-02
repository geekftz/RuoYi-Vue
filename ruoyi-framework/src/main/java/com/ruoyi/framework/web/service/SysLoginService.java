package com.ruoyi.framework.web.service;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.BlackListException;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.exception.user.UserNotExistsException;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 登录校验服务
 * <p>
 * 核心职责：处理用户登录的全流程校验逻辑，包括验证码校验、账号密码校验、IP黑名单校验，
 * 最终通过Spring Security的AuthenticationManager完成身份认证并生成JWT令牌。
 * </p>
 * <p>
 * 登录流程：
 * 1. validateCaptcha —— 校验验证码是否正确且未过期
 * 2. loginPreCheck —— 校验用户名密码格式、IP黑名单
 * 3. authenticationManager.authenticate —— 委托Spring Security进行身份认证
 *    （内部调用UserDetailsServiceImpl.loadUserByUsername加载用户，SysPasswordService.validate校验密码）
 * 4. recordLoginInfo —— 异步更新数据库中的登录IP和登录时间
 * 5. tokenService.createToken —— 生成JWT令牌返回给前端
 * </p>
 *
 * @author ruoyi
 */
@Component
public class SysLoginService
{
    /** 令牌服务 */
    @Autowired
    private TokenService tokenService;

    /** Spring Security 认证管理器 */
    @Resource
    private AuthenticationManager authenticationManager;

    /** Redis 缓存 */
    @Autowired
    private RedisCache redisCache;

    /** 用户业务层 */
    @Autowired
    private ISysUserService userService;

    /** 系统参数配置业务层 */
    @Autowired
    private ISysConfigService configService;

    /**
     * 用户登录验证（登录接口核心方法）
     * <p>
     * 完整登录流程：
     * 1. 验证码校验 —— 确保验证码正确且未过期，防止暴力破解
     * 2. 登录前置校验 —— 校验用户名密码格式、IP黑名单
     * 3. Spring Security认证 —— 构建UsernamePasswordAuthenticationToken，
     *    交由AuthenticationManager认证（内部调用UserDetailsServiceImpl加载用户、SysPasswordService校验密码）
     * 4. 异步记录登录日志 —— 无论成功失败都记录登录信息到sys_logininfor表
     * 5. 更新数据库登录信息 —— 记录登录IP和登录时间
     * 6. 生成JWT令牌 —— 返回给前端用于后续请求认证
     * </p>
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     验证码唯一标识（用于从Redis获取对应的验证码）
     * @return JWT令牌字符串
     */
    public String login(String username, String password, String code, String uuid)
    {
        // 步骤1：验证码校验（从Redis取出验证码比对，校验通过后删除缓存）
        validateCaptcha(username, code, uuid);
        // 步骤2：登录前置校验（用户名密码格式、IP黑名单）
        loginPreCheck(username, password);
        // 步骤3：Spring Security 身份认证
        Authentication authentication = null;
        try
        {
            // 构建用户名密码认证令牌
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            // 存入ThreadLocal，供后续SysPasswordService从上下文获取用户名密码
            AuthenticationContextHolder.setContext(authenticationToken);
            // 委托AuthenticationManager进行认证：
            // 内部会调用UserDetailsServiceImpl.loadUserByUsername加载用户信息，
            // 再调用SysPasswordService.validate校验密码是否匹配
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                // 密码错误：异步记录登录失败日志，抛出密码不匹配异常
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                // 其他异常（用户不存在、已删除、已停用等）：异步记录日志并抛出业务异常
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            // 清除ThreadLocal中的认证上下文，防止内存泄漏
            AuthenticationContextHolder.clearContext();
        }
        // 步骤4：异步记录登录成功日志
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        // 从认证结果中获取LoginUser对象
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 步骤5：更新数据库中的登录IP和登录时间
        recordLoginInfo(loginUser.getUserId());
        // 步骤6：生成并返回JWT令牌
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     * <p>
     * 业务流程：
     * 1. 从系统配置中检查是否启用了验证码功能
     * 2. 启用则从Redis中取出验证码（key为captcha_codes:{uuid}）
     * 3. 验证码不存在表示已过期，抛出CaptchaExpireException
     * 4. 验证码使用后立即删除（一次性使用，防止重复利用）
     * 5. 比对验证码不区分大小写，不匹配抛出CaptchaException
     * </p>
     *
     * @param username 用户名（用于记录登录日志）
     * @param code     用户输入的验证码
     * @param uuid     验证码唯一标识（前端生成，用于从Redis中获取对应的验证码）
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        // 检查系统是否启用了验证码功能（sys.account.captchaEnabled配置项）
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            // 拼接Redis缓存key：captcha_codes:{uuid}
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            // 从Redis中获取验证码文本
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                // 验证码已过期（Redis缓存已失效）
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            // 验证码一次性使用，验证后立即删除防止重复利用
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                // 验证码不匹配（忽略大小写）
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登录前置校验（在Spring Security认证之前进行的基础校验）
     * <p>
     * 校验内容：
     * 1. 用户名和密码是否为空
     * 2. 密码长度是否在允许范围内（防止超长密码导致性能问题）
     * 3. 用户名长度是否在允许范围内
     * 4. 当前IP是否在登录黑名单中
     * </p>
     * <p>
     * 设计说明：这些校验在Spring Security认证之前执行，可以提前拦截非法请求，
     * 避免不必要的数据库查询和密码加密运算。
     * </p>
     *
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(String username, String password)
    {
        // 校验1：用户名或密码不能为空
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 校验2：密码长度必须在允许范围内（默认5~20位）
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 校验3：用户名长度必须在允许范围内（默认2~20位）
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 校验4：IP黑名单校验（从系统配置sys.login.blackIPList获取黑名单IP列表）
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     * 记录登录信息到数据库
     * <p>
     * 更新sys_user表中的login_ip（登录IP）和login_date（登录时间）字段，
     * 用于安全审计和用户活跃度统计。
     * </p>
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        userService.updateLoginInfo(userId, IpUtils.getIpAddr(), DateUtils.getNowDate());
    }
}
