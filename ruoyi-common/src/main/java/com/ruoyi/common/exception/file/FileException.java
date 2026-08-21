package com.ruoyi.common.exception.file;

import com.ruoyi.common.exception.base.BaseException;

/**
 * 文件信息异常类
 * <p>
 * 【架构位置】exception/file，文件上传相关异常的父类，继承BaseException从而获得i18n国际化消息能力。
 * 【设计说明】构造时固定传入module="file"，表示错误消息从i18n属性文件的file模块（如 upload.* 前缀key）中查找。
 * 【触发场景】文件上传校验失败（文件名过长、大小超限）时，由FileUploadUtils抛出，最终被全局异常处理器捕获并提示前端。
 * 
 * @author ruoyi
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    /**
     * @param code 国际化消息key（对应messages.properties中的 upload.xxx 条目）
     * @param args 消息中的占位参数（如最大长度值、最大大小值）
     */
    public FileException(String code, Object[] args)
    {
        // module固定为"file"，defaultMessage传null表示必须能从i18n文件中找到code对应文案
        super("file", code, args, null);
    }

}
