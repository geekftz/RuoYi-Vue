package com.ruoyi.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 错误信息处理类。
 * <p>
 * 【能力】从异常对象中提取有用的错误信息字符串。
 * 【两个方法的区别】
 * - getExceptionMessage：获取完整堆栈信息（包含所有Caused by链），适合记录到日志文件排查问题
 * - getRootErrorMessage：只获取最底层根因异常的message，适合作为错误提示返回给前端
 * 【使用场景】全局异常处理器GlobalExceptionHandler中用getRootErrorMessage提取根因消息，
 * 避免把冗长的堆栈信息直接返回给前端用户。
 * 
 * @author ruoyi
 */
public class ExceptionUtil
{
    /**
     * 获取exception的详细错误信息（完整堆栈，含所有Caused by链）。
     * 
     * @param e 异常对象
     * @return 完整堆栈信息的字符串，等同于e.printStackTrace()打印到控制台的内容
     */
    public static String getExceptionMessage(Throwable e)
    {
        // 用StringWriter+PrintWriter把堆栈信息写入内存字符串（而不是打印到控制台）
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }

    /**
     * 获取根因异常的message（刨掉中间层包装，直达问题本质）。
     * 例：ServiceException → SQLException → "Duplicate entry 'admin' for key"，最终返回最后一条
     * 
     * @param e 异常对象
     * @return 根因异常的message字符串
     */
    public static String getRootErrorMessage(Exception e)
    {
        // Apache Commons Lang3的工具：递归获取异常链最底层的根因
        Throwable root = ExceptionUtils.getRootCause(e);
        root = (root == null ? e : root);
        if (root == null)
        {
            return "";
        }
        String msg = root.getMessage();
        if (msg == null)
        {
            return "null";
        }
        // StringUtils.defaultString：msg为null时返回""，防止NPE
        return StringUtils.defaultString(msg);
    }
}
