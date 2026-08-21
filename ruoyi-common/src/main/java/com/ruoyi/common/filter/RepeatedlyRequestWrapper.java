package com.ruoyi.common.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import com.ruoyi.common.utils.http.HttpHelper;
import com.ruoyi.common.constant.Constants;

/**
 * 构建可重复读取inputStream的request
 * <p>
 * 【架构位置】filter包，RepeatableFilter的内部实现，继承HttpServletRequestWrapper（装饰器模式）。
 * 【核心原理】构造时把请求体body一次性读入byte[]数组缓存，
 * 之后每次调用getInputStream()/getReader()都基于缓存的byte[]创建新流，实现body可重复读。
 * 【应用场景】防重复提交拦截器读body比对参数 → @Log切面读body记录日志 → @RequestBody读body绑定对象，三方共存。
 * 【使用方式】不需要手动new，由RepeatableFilter自动包装，开发者无感知。
 * 
 * @author ruoyi
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper
{
    /** 缓存的请求体字节数组，后续所有读取都从这里取 */
    private final byte[] body;

    /**
     * 构造时立即把body读入内存：
     * 1. 设置请求/响应编码为UTF-8防止中文乱码
     * 2. 通过HttpHelper.getBodyString读取整个body并转为字节数组缓存
     */
    public RepeatedlyRequestWrapper(HttpServletRequest request, ServletResponse response) throws IOException
    {
        super(request);
        request.setCharacterEncoding(Constants.UTF8);
        response.setCharacterEncoding(Constants.UTF8);

        // 关键：一次性读取body缓存起来，之后每次getInputStream()都从缓存创建新流
        body = HttpHelper.getBodyString(request).getBytes(Constants.UTF8);
    }

    /**
     * 重写getReader()：基于缓存body创建BufferedReader，实现可重复读
     */
    @Override
    public BufferedReader getReader() throws IOException
    {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 重写getInputStream()：每次都new一个ByteArrayInputStream，
     * 调用方（拦截器/切面/Controller）各自拿到独立的新流，互不影响，实现body可重复读
     */
    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        // 基于缓存的body字节数组创建新流，每次调用都返回新实例
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        // 返回ServletInputStream匿名实现，底层读的是ByteArrayInputStream
        return new ServletInputStream()
        {
            @Override
            public int read() throws IOException
            {
                return bais.read();
            }

            @Override
            public int available() throws IOException
            {
                return body.length;
            }

            @Override
            public boolean isFinished()
            {
                return false;
            }

            @Override
            public boolean isReady()
            {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener)
            {

            }
        };
    }
}
