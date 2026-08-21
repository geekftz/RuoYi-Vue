package com.ruoyi.common.utils.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用http工具封装
 * <p>
 * 【能力】从HttpServletRequest中读取完整的请求体（body）字符串。
 * 【使用场景】RepeatedlyRequestWrapper构造时调用本方法缓存body；@Log切面中读取请求参数。
 * 【原理】通过request.getInputStream()逐行读取body内容，拼成String返回。
 * 【注意】如果请求body已被RepeatableFilter包装过，本方法读到的是缓存的byte[]而非原始流，可重复调用。
 * 
 * @author ruoyi
 */
public class HttpHelper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

    /**
     * 读取请求体完整内容为字符串
     * 
     * @param request HTTP请求对象
     * @return 请求体字符串（JSON或表单参数），读取失败返回空字符串
     */
    public static String getBodyString(ServletRequest request)
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        // try-with-resources自动关闭inputStream
        try (InputStream inputStream = request.getInputStream())
        {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = "";
            // 逐行读取body内容（JSON请求体通常只有一行）
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("getBodyString出现问题！");
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    LOGGER.error(ExceptionUtils.getMessage(e));
                }
            }
        }
        return sb.toString();
    }
}
