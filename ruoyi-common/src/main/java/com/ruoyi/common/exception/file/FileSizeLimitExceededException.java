package com.ruoyi.common.exception.file;

/**
 * 文件名大小限制异常类
 * <p>
 * 【触发场景】上传文件大小超过上限（默认50MB，可在application.yml的spring.servlet.multipart.max-file-size调整），
 * FileUploadUtils断言大小后抛出，提示前端"上传的文件大小超出限制的文件大小！"。
 * 【前端联动】前端上传组件通常也会做大小预校验，这里是后端兜底校验，防止绕过前端直接调接口。
 * 
 * @author ruoyi
 */
public class FileSizeLimitExceededException extends FileException
{
    private static final long serialVersionUID = 1L;

    /**
     * @param defaultMaxSize 允许的最大文件大小（单位：字节），作为参数填入i18n消息模板
     */
    public FileSizeLimitExceededException(long defaultMaxSize)
    {
        // i18n消息key，文案模板中会引用{0}占位符显示最大允许大小
        super("upload.exceed.maxSize", new Object[] { defaultMaxSize });
    }
}
