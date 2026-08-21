package com.ruoyi.common.utils.sign;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Md5加密方法
 * <p>
 * 【能力】计算字符串的MD5哈希值（32位十六进制字符串）。
 * 【使用场景】文件完整性校验、密码辅助加密（若依主密码加密用的是BCrypt，见SecurityUtils）。
 * 【使用示例】String hash = Md5Utils.hash("admin123"); // "0192023a7bbd73250516f069df18b500"
 * 【安全提示】MD5已被证明不安全（可碰撞、可彩虹表破解），不适合单独用于密码存储，
 * 若依的用户密码使用BCrypt加密（SecurityUtils.encryptPassword），本类仅用于非安全场景的哈希计算。
 * 
 * @author ruoyi
 */
public class Md5Utils
{
    private static final Logger log = LoggerFactory.getLogger(Md5Utils.class);

    /**
     * 计算字符串的MD5摘要（返回字节数组）
     * 
     * @param s 原始字符串
     * @return MD5摘要字节数组
     */
    private static byte[] md5(String s)
    {
        MessageDigest algorithm;
        try
        {
            algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(s.getBytes("UTF-8"));
            byte[] messageDigest = algorithm.digest();
            return messageDigest;
        }
        catch (Exception e)
        {
            log.error("MD5 Error...", e);
        }
        return null;
    }

    /**
     * 字节数组 → 十六进制字符串（每个byte转2位hex）
     * 
     * @param hash MD5摘要字节数组
     * @return 32位十六进制字符串
     */
    private static final String toHex(byte hash[])
    {
        if (hash == null)
        {
            return null;
        }
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++)
        {
            // 不足两位前补0，保证固定32位长度
            if ((hash[i] & 0xff) < 0x10)
            {
                buf.append("0");
            }
            buf.append(Long.toString(hash[i] & 0xff, 16));
        }
        return buf.toString();
    }

    /**
     * 对外暴露的MD5哈希方法（最常用入口）
     * 
     * @param s 原始字符串
     * @return 32位MD5哈希值（十六进制字符串）
     */
    public static String hash(String s)
    {
        try
        {
            return new String(toHex(md5(s)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            log.error("not supported charset...{}", e);
            return s;
        }
    }
}
