package com.ruoyi.common.utils;

/**
 * 处理并记录日志文件
 * <p>
 * 【能力】把日志消息包装成 [消息内容] 的格式，让日志输出更规范易读。
 * 【使用场景】在@Log切面的日志记录中，把操作人、模块名等信息用[]包裹后再拼接输出。
 * 【使用示例】LogUtils.getBlock("系统管理") 返回 "[系统管理]"
 * 
 * @author ruoyi
 */
public class LogUtils
{
    /**
     * 把消息用[]包裹：null转为空字符串
     * 
     * @param msg 日志消息内容
     * @return 格式为 [msg] 的字符串
     */
    public static String getBlock(Object msg)
    {
        if (msg == null)
        {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}
