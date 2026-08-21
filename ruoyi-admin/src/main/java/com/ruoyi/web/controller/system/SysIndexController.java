package com.ruoyi.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;

/**
 * 首页
 * <p>
 * 【架构位置】ruoyi-admin / controller层 —— 系统级杂项接口。
 * 【所属业务】系统首页提示 + 锁屏解锁（前端锁定屏幕功能的后端校验接口）。
 *
 * @author ruoyi
 */
@RestController
public class SysIndexController
{
    /** 系统基础配置 */
    @Autowired
    private RuoYiConfig ruoyiConfig;

    /** 用户服务：解锁时按用户名查用户校验密码 */
    @Autowired
    private ISysUserService userService;

    /**
     * 访问首页，提示语
     * <p>
     * 直接访问后端根路径 / 时的提示（返回纯文本，不是JSON）；
     * 前后端分离架构下正常用户不会访问到这里，仅作存活探针/提示用途
     */
    @RequestMapping("/")
    public String index()
    {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", ruoyiConfig.getName(), ruoyiConfig.getVersion());
    }

    /**
     * 解锁屏幕
     * <p>
     * 【业务场景】前端"锁屏"功能：用户暂时离开时锁定界面，回来输入当前登录账号的密码解锁。
     * 后端做的事：取当前登录用户名 → 查库 → 用BCrypt比对用户输入的密码与库中密文。
     *
     * @param body 【@RequestBody】前端JSON请求体 {"password":"xxx"}
     * @return AjaxResult 统一返回体：{"code":200,"msg":"解锁成功"} / 各失败提示
     */
    @PostMapping("/unlockscreen")
    public AjaxResult unlockScreen(@RequestBody Map<String, String> body)
    {
        String password = body.get("password");
        if (StringUtils.isEmpty(password))
        {
            return AjaxResult.error("密码不能为空");
        }
        // SecurityUtils.getUsername()：从SecurityContext取当前登录用户名（JWT过滤器已放入）
        String username = SecurityUtils.getUsername();
        SysUser user = userService.selectUserByUserName(username);
        if (user == null)
        {
            return AjaxResult.error("服务器超时，请重新登录");
        }
        // matchesPassword：BCrypt比对明文与密文（不解密，重新加密后比对哈希）
        if (!SecurityUtils.matchesPassword(password, user.getPassword()))
        {
            return AjaxResult.error("密码错误，请重新输入");
        }

        return AjaxResult.success("解锁成功");
    }
}
