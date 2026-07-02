package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.SysRegisterService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 用户注册控制器
 * <p>
 * 提供用户自助注册功能。当系统配置开启注册功能时（sys.account.registerUser=true），
 * 允许用户通过注册接口创建新账号。
 * </p>
 *
 * @author ruoyi
 */
@RestController
public class SysRegisterController extends BaseController
{
    /** 注册校验服务 */
    @Autowired
    private SysRegisterService registerService;

    /** 系统参数配置业务层 */
    @Autowired
    private ISysConfigService configService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}
