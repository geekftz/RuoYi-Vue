package com.ruoyi.common.utils.file;

/**
 * 媒体类型工具类
 * <p>
 * 【能力】定义了文件上传允许的各种扩展名常量数组和MIME类型映射。
 * 【使用场景】文件上传时校验文件类型是否合法（FileUploadUtils.isAllowedExtension）。
 * 【关键常量】
 * - IMAGE_EXTENSION：允许上传的图片格式 [bmp, gif, jpg, jpeg, png]
 * - DEFAULT_ALLOWED_EXTENSION：默认允许的所有文件格式（图片+办公文档+压缩包+视频+PDF）
 * 【安全设计】通过白名单机制限制可上传的文件类型，防止上传恶意脚本文件（如.jsp、.exe）。
 * 
 * @author ruoyi
 */
public class MimeTypeUtils
{
    public static final String IMAGE_PNG = "image/png";

    public static final String IMAGE_JPG = "image/jpg";

    public static final String IMAGE_JPEG = "image/jpeg";

    public static final String IMAGE_BMP = "image/bmp";

    public static final String IMAGE_GIF = "image/gif";
    
    public static final String[] IMAGE_EXTENSION = { "bmp", "gif", "jpg", "jpeg", "png" };

    public static final String[] FLASH_EXTENSION = { "swf", "flv" };

    public static final String[] MEDIA_EXTENSION = { "swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg",
            "asf", "rm", "rmvb" };

    public static final String[] VIDEO_EXTENSION = { "mp4", "avi", "rmvb" };

    /** 默认允许上传的文件扩展名白名单（按用途分类注释） */
    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png",
            // word excel powerpoint
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
            // 压缩文件
            "rar", "zip", "gz", "bz2",
            // 视频格式
            "mp4", "avi", "rmvb",
            // pdf
            "pdf" };

    /**
     * 根据MIME类型获取对应的文件扩展名
     * 
     * @param prefix MIME类型（如"image/png"）
     * @return 扩展名（如"png"），未匹配返回空字符串
     */
    public static String getExtension(String prefix)
    {
        switch (prefix)
        {
            case IMAGE_PNG:
                return "png";
            case IMAGE_JPG:
                return "jpg";
            case IMAGE_JPEG:
                return "jpeg";
            case IMAGE_BMP:
                return "bmp";
            case IMAGE_GIF:
                return "gif";
            default:
                return "";
        }
    }
}
