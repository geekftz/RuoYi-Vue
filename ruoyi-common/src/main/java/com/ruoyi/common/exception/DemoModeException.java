package com.ruoyi.common.exception;

/**
 * 演示模式异常
 * <p>
 * 【定位】若依官方演示站专用：演示环境禁止增删改操作（防止数据被乱改），拦截器检测到写操作就抛本异常。
 * 【触发】SysUserController等接口的add/edit/remove方法开头有 demoMode() 检查，演示账号操作时抛出。
 * 自建项目部署时不用管此异常。
 * 
 * @author ruoyi
 */
public class DemoModeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public DemoModeException()
    {
    }
}
