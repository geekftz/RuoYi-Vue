package com.ruoyi.common.exception.file;

import java.util.Arrays;

/**
 * 文件上传无效扩展名异常类
 * <p>
 * 【触发场景】前端上传的文件后缀不在白名单内时抛出（如上传.exe/.jsp等危险文件被拦截）。
 * FileUploadUtils.isAllowedExtension校验失败 → 抛出本异常 → 全局异常处理器返回提示"文件后缀不正确"。
 * 【内部类设计】按文件类型细分4个静态内部类（图片/Flash/媒体/视频），
 * 便于不同上传场景（头像上传、富文本编辑器上传）抛出语义更明确的异常，全局异常处理器可针对性处理。
 * 
 * @author ruoyi
 */
public class InvalidExtensionException extends FileUploadException
{
    private static final long serialVersionUID = 1L;

    /** 允许上传的扩展名白名单（如 ["jpg","png","gif"]） */
    private String[] allowedExtension;
    /** 实际上传文件的扩展名（不合规的那个） */
    private String extension;
    /** 实际上传的文件名 */
    private String filename;

    /**
     * 构造时直接把错误信息拼装好：文件名 + 实际后缀 + 允许的后缀列表，用户一看就懂
     */
    public InvalidExtensionException(String[] allowedExtension, String extension, String filename)
    {
        super("文件[" + filename + "]后缀[" + extension + "]不正确，请上传" + Arrays.toString(allowedExtension) + "格式");
        this.allowedExtension = allowedExtension;
        this.extension = extension;
        this.filename = filename;
    }

    public String[] getAllowedExtension()
    {
        return allowedExtension;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getFilename()
    {
        return filename;
    }

    public static class InvalidImageExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        public InvalidImageExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidFlashExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        public InvalidFlashExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidMediaExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        public InvalidMediaExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidVideoExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        public InvalidVideoExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }
}
