package com.ruoyi.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 获取i18n资源文件
 * <p>
 * 【架构位置】ruoyi-common / utils层 —— 国际化文案读取工具，配合 I18nConfig 使用。
 * 【使用场景】后端需要返回多语言提示语时：MessageUtils.message("user.register.success")
 * → 读取 ruoyi-admin/resources/i18n/messages.properties（及对应语言变体）中的文案，
 * 语言由 LocaleContextHolder（请求级，可用 ?lang=xx 切换）决定。
 *
 * @author ruoyi
 */
public class MessageUtils
{
    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param code 消息键（对应 messages.properties 中的key，如 user.login.success）
     * @param args 参数（文案中的占位符 {0} {1} 替换值）
     * @return 获取国际化翻译值
     */
    public static String message(String code, Object... args)
    {
        // SpringUtils.getBean：在非注入环境（静态方法）中手动从Spring容器取Bean的若依封装
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
