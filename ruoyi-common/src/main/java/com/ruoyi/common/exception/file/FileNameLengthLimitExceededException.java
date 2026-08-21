package com.ruoyi.common.exception.file;

/**
 * 文件名称超长限制异常类
 * <p>
 * 【触发场景】前端上传文件时文件名长度超过限制（默认50字符，见FileUploadUtils.DEFAULT_FILE_NAME_LENGTH），
 * FileUploadUtils.extractFilename校验后抛出。前端会收到"上传的文件名最长50个字符"类提示。
 * 
 * @author ruoyi
 */
public class FileNameLengthLimitExceededException extends FileException
{
    private static final long serialVersionUID = 1L;

    /**
     * @param defaultFileNameLength 允许的最大文件名长度，作为参数填入i18n消息模板
     */
    public FileNameLengthLimitExceededException(int defaultFileNameLength)
    {
        // "upload.filename.exceed.length"是i18n消息key，对应文案如：upload.filename.exceed.length=upload filename max length {0}
        super("upload.filename.exceed.length", new Object[] { defaultFileNameLength });
    }
}
