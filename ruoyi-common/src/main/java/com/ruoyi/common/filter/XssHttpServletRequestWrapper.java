package com.ruoyi.common.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.EscapeUtil;

/**
 * XSS过滤处理
 * <p>
 * 【架构位置】filter包，XssFilter的内部实现，继承HttpServletRequestWrapper（装饰器模式）。
 * 【核心原理】重写getParameterValues()和getInputStream()两个方法，
 * 在返回值之前先用EscapeUtil.clean()清洗XSS字符，调用方拿到的就是清洗后的安全数据。
 * 【清洗范围】
 * 1. 表单参数（getParameterValues）：适用于form表单提交
 * 2. JSON请求体（getInputStream）：适用于axios默认的application/json提交
 * 【清洗方式】EscapeUtil.clean()会把<script>转义为&lt;script&gt;，浏览器展示时当纯文本不会执行。
 * 
 * @author ruoyi
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper
{
    /**
     * @param request 原始HttpServletRequest
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request)
    {
        super(request);
    }

    /**
     * 重写获取参数值方法：对所有表单参数值进行XSS清洗和去空格
     * 
     * @param name 参数名
     * @return 清洗后的参数值数组
     */
    @Override
    public String[] getParameterValues(String name)
    {
        String[] values = super.getParameterValues(name);
        if (values != null)
        {
            int length = values.length;
            String[] escapesValues = new String[length];
            for (int i = 0; i < length; i++)
            {
                // 防xss攻击和过滤前后空格（EscapeUtil.clean转义脚本字符，trim去首尾空格）
                escapesValues[i] = EscapeUtil.clean(values[i]).trim();
            }
            return escapesValues;
        }
        return super.getParameterValues(name);
    }

    /**
     * 重写获取请求体流：对JSON请求体进行XSS清洗
     * 非JSON请求（如文件上传）直接放行不处理
     */
    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        // 非json类型，直接返回（文件上传等场景不做XSS清洗，防止破坏二进制内容）
        if (!isJsonRequest())
        {
            return super.getInputStream();
        }

        // 为空，直接返回
        String json = IOUtils.toString(super.getInputStream(), "utf-8");
        if (StringUtils.isEmpty(json))
        {
            return super.getInputStream();
        }

        // xss过滤：清洗JSON字符串中的恶意脚本字符
        json = EscapeUtil.clean(json).trim();
        byte[] jsonBytes = json.getBytes("utf-8");
        // 把清洗后的JSON重新封装成ServletInputStream返回给下游（@RequestBody读取的就是清洗后的数据）
        final ByteArrayInputStream bis = new ByteArrayInputStream(jsonBytes);
        return new ServletInputStream()
        {
            @Override
            public boolean isFinished()
            {
                return true;
            }

            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public int available() throws IOException
            {
                return jsonBytes.length;
            }

            @Override
            public void setReadListener(ReadListener readListener)
            {
            }

            @Override
            public int read() throws IOException
            {
                return bis.read();
            }
        };
    }

    /**
     * 是否是Json请求（通过Content-Type头判断）
     * 
     * @return true=application/json请求，false=其他类型
     */
    public boolean isJsonRequest()
    {
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        return StringUtils.startsWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
    }
}