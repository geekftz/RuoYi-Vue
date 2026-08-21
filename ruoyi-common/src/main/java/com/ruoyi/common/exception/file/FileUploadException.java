package com.ruoyi.common.exception.file;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 文件上传异常类
 * <p>
 * 【架构位置】exception/file，文件扩展名校验相关异常的基类（InvalidExtensionException继承本类）。
 * 【设计特点】
 * 1. 注意：继承的是java.lang.Exception（受检异常），与FileException（继承BaseException→RuntimeException）不同分支，
 *    若依文件校验体系实际走FileException分支，本类主要作为扩展名校验异常树（InvalidExtensionException）的根。
 * 2. 重写了printStackTrace/getCause：因为父类Exception的cause字段只能通过构造器传入，
 *    本类用自己的cause字段保存原始异常，保证打印堆栈时能看到完整因果链（Caused by: ...）。
 * 
 * @author ruoyi
 */
public class FileUploadException extends Exception
{

    private static final long serialVersionUID = 1L;

    /** 原始异常（真正的底层原因），重写getCause()返回它 */
    private final Throwable cause;

    public FileUploadException()
    {
        this(null, null);
    }

    public FileUploadException(final String msg)
    {
        this(msg, null);
    }

    /**
     * @param msg 异常描述信息（会作为错误提示返回给前端）
     * @param cause 底层原始异常（如IO异常），用于排查问题时打印完整堆栈
     */
    public FileUploadException(String msg, Throwable cause)
    {
        super(msg);
        this.cause = cause;
    }

    /**
     * 打印堆栈到输出流：先打印本异常，再追加打印底层cause的堆栈，保证日志中因果链完整
     */
    @Override
    public void printStackTrace(PrintStream stream)
    {
        super.printStackTrace(stream);
        if (cause != null)
        {
            stream.println("Caused by:");
            cause.printStackTrace(stream);
        }
    }

    /**
     * 打印堆栈到Writer：与上面同理，适配日志框架使用Writer的场景
     */
    @Override
    public void printStackTrace(PrintWriter writer)
    {
        super.printStackTrace(writer);
        if (cause != null)
        {
            writer.println("Caused by:");
            cause.printStackTrace(writer);
        }
    }

    /**
     * 返回底层原始异常（覆盖父类实现，因为父类的cause没有通过构造器设置）
     */
    @Override
    public Throwable getCause()
    {
        return cause;
    }
}
