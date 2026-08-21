package com.ruoyi.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 注册校验方法
 * <p>
 * 【架构位置】ruoyi-framework / web / service层 —— 用户自助注册业务服务。
 * 【调用链路】前端注册页 POST /register → SysLoginController.register（@Anonymous免登录）
 * → 本类 register()：校验验证码 → 校验用户名/密码规则 → 密码BCrypt加密 → 落库 → 异步记录注册日志。
 * 【开关】系统是否开放注册由参数配置 sys.account.registerUser 控制（系统管理-参数设置）。
 *
 * @author ruoyi
 */
@Component
public class SysRegisterService
{
    /** 用户服务：校验用户名唯一性、执行注册入库 */
    @Autowired
    private ISysUserService userService;

    /** 参数配置服务：读取验证码开关（sys.account.captchaEnabled） */
    @Autowired
    private ISysConfigService configService;

    /** 若依封装的Redis工具：读取/删除验证码缓存 */
    @Autowired
    private RedisCache redisCache;

    /**
     * 注册
     * <p>
     * @param registerBody 前端提交的注册表单（username/password/code/uuid）
     * @return 空串=注册成功；非空=失败原因提示语（由Controller包成AjaxResult返回前端）
     */
    public String register(RegisterBody registerBody)
    {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 验证码开关：从参数配置读取，开启则先校验验证码再往下走
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        // 逐项校验：任一不通过即返回提示语（若依风格：业务校验用返回msg而非抛异常）
        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = "账户长度必须在2到20个字符之间";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        else if (!userService.checkUserNameUnique(sysUser))
        {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            // 校验全部通过：昵称默认用账号；记录密码修改时间；密码BCrypt强散列加密后入库（绝不存明文）
            sysUser.setNickName(username);
            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag)
            {
                msg = "注册失败,请联系系统管理人员";
            }
            else
            {
                // 异步写登录日志表 sys_logininfor（AsyncManager后台线程执行，不阻塞注册响应）
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     * <p>
     * 从Redis按 uuid 取验证码与用户输入比对；无论成败取完即删（防暴力多次尝试同一验证码）
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        // 缓存中不存在：验证码已过期或uuid伪造
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        // 不区分大小写比对，不一致抛 CaptchaException（由全局异常处理器转为友好提示返回前端）
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }
}
