package com.ruoyi.common.utils.file;

import java.io.File;
import org.apache.commons.lang3.StringUtils;

/**
 * 文件类型工具类
 * <p>
 * 【能力】从文件名或文件字节码中提取文件扩展名（后缀）。
 * 【使用场景】文件上传时校验文件类型是否在白名单中；头像上传时判断图片格式。
 * 【方法速查】
 * - getFileType(file/fileName)：从文件名提取扩展名，如"ruoyi.txt"→"txt"
 * - getFileExtendName(photoByte)：从文件字节码（魔数）判断图片真实格式，防止改后缀伪装
 * 【安全提示】getFileExtendName通过读取文件头部的魔数（Magic Number）判断真实格式，
 * 比单纯看文件后缀更安全（防止把.exe改名为.jpg上传）。
 *
 * @author ruoyi
 */
public class FileTypeUtils
{
    /**
     * 获取文件类型
     * <p>
     * 例如: ruoyi.txt, 返回: txt
     * 
     * @param file 文件名
     * @return 后缀（不含".")
     */
    public static String getFileType(File file)
    {
        if (null == file)
        {
            return StringUtils.EMPTY;
        }
        return getFileType(file.getName());
    }

    /**
     * 获取文件类型
     * <p>
     * 例如: ruoyi.txt, 返回: txt
     *
     * @param fileName 文件名
     * @return 后缀（不含".")
     */
    public static String getFileType(String fileName)
    {
        int separatorIndex = fileName.lastIndexOf(".");
        if (separatorIndex < 0)
        {
            return "";
        }
        return fileName.substring(separatorIndex + 1).toLowerCase();
    }

    /**
     * 获取文件类型（通过文件字节码头部的魔数判断真实格式）
     * 【安全用途】防止恶意用户把可执行文件改名为图片后缀上传，
     * 通过读取文件二进制头部特征码判断真实格式：
     * GIF: 47 49 46 38 (GIF8)、JPG: FF D8 FF + JFIF标记、BMP: 42 4D (BM)、PNG: 89 50 4E 47 (.PNG)
     * 
     * @param photoByte 文件字节码
     * @return 后缀（不含".")
     */
    public static String getFileExtendName(byte[] photoByte)
    {
        String strFileExtendName = "JPG";
        if ((photoByte[0] == 71) && (photoByte[1] == 73) && (photoByte[2] == 70) && (photoByte[3] == 56)
                && ((photoByte[4] == 55) || (photoByte[4] == 57)) && (photoByte[5] == 97))
        {
            strFileExtendName = "GIF";
        }
        else if ((photoByte[6] == 74) && (photoByte[7] == 70) && (photoByte[8] == 73) && (photoByte[9] == 70))
        {
            strFileExtendName = "JPG";
        }
        else if ((photoByte[0] == 66) && (photoByte[1] == 77))
        {
            strFileExtendName = "BMP";
        }
        else if ((photoByte[1] == 80) && (photoByte[2] == 78) && (photoByte[3] == 71))
        {
            strFileExtendName = "PNG";
        }
        return strFileExtendName;
    }
}