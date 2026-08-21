package com.ruoyi.common.exception.base;

import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 基础异常
 * <p>
 * 【架构位置】exception/base，带国际化（i18n）能力的异常基类，用户域异常树（UserException及其子类）的根。
 * 【核心机制】code不是数字错误码，而是messages.properties中的消息key（如"user.password.retry.limit.exceed"）；
 * getMessage()时通过MessageUtils.message(code, args)查出对应文案并用args填充占位符，
 * 实现"异常消息多语言"——登录失败提示可随系统语言切换。
 * 【与ServiceException分工】ServiceException直接带中文消息、简单直接，日常业务首选；
 * BaseException走i18n key，适合需要多语言的面向终端用户场景（登录/注册）。
 * 
 * @author ruoyi
 */
public class BaseException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;

    public BaseException(String module, String code, Object[] args, String defaultMessage)
    {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    public BaseException(String module, String code, Object[] args)
    {
        this(module, code, args, null);
    }

    public BaseException(String module, String defaultMessage)
    {
        this(module, null, null, defaultMessage);
    }

    public BaseException(String code, Object[] args)
    {
        this(null, code, args, null);
    }

    public BaseException(String defaultMessage)
    {
        this(null, null, null, defaultMessage);
    }

    @Override
    public String getMessage()
    {
        String message = null;
        // 【i18n核心】有code则按code查国际化文案并填充args参数；查不到（如properties没配该key）则回退默认消息
        if (!StringUtils.isEmpty(code))
        {
            message = MessageUtils.message(code, args);
        }
        if (message == null)
        {
            message = defaultMessage;
        }
        return message;
    }

    public String getModule()
    {
        return module;
    }

    public String getCode()
    {
        return code;
    }

    public Object[] getArgs()
    {
        return args;
    }

    public String getDefaultMessage()
    {
        return defaultMessage;
    }
}
