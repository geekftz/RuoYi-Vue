package com.ruoyi.common.utils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 时间工具类
 * <p>
 * 【架构位置】common/utils，全系统日期处理的核心工具类，继承Apache Commons Lang3的DateUtils获得基础能力。
 * 【使用场景】全系统高频使用：实体类时间字段格式化、文件上传路径按日期分目录、定时任务日志记录时间等。
 * 【核心方法速查】
 * - getNowDate()：获取当前时间Date对象
 * - getTime()：获取当前时间字符串 "yyyy-MM-dd HH:mm:ss"
 * - dateTimeNow()：获取当前时间字符串 "yyyyMMddHHmmss"（常用于生成文件名/编号）
 * - datePath()：获取日期路径 "yyyy/MM/dd"（文件上传按日期分目录）
 * - parseDateToStr(format, date)：Date → 字符串
 * - dateTime(format, ts)：字符串 → Date
 * - parseDate(str)：智能解析多种格式的日期字符串
 * - timeDistance(end, start)：计算两个时间的差值，返回"X天X小时X分钟"
 * 【前端联动】前端传递日期参数时格式必须与后端约定一致，后端用parseDate解析时自动尝试多种格式。
 * 
 * @author ruoyi
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils
{
    /** 常用日期格式常量，避免散落各处的魔法字符串 */
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /** 智能解析支持的日期格式数组：parseDate()会依次尝试这些格式直到解析成功 */
    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", 
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     * 
     * @return Date() 当前日期
     */
    public static Date getNowDate()
    {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     * 
     * @return String
     */
    public static String getDate()
    {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * 获取当前日期时间字符串, 格式为yyyy-MM-dd HH:mm:ss
     * 【若依高频用法】日志记录、操作时间填充
     * 
     * @return 如 "2024-01-15 14:30:00"
     */
    public static final String getTime()
    {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当前日期时间字符串, 格式为yyyyMMddHHmmss
     * 【若依高频用法】生成文件名、订单号等需要时间戳的场景
     * 
     * @return 如 "20240115143000"
     */
    public static final String dateTimeNow()
    {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    /**
     * 获取当前时间的指定格式字符串
     * 
     * @param format 日期格式（如YYYY_MM_DD）
     * @return 格式化后的当前时间字符串
     */
    public static final String dateTimeNow(final String format)
    {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date)
    {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    /**
     * Date → 指定格式字符串（核心转换方法，被多个方法调用）
     * 
     * @param format 目标格式
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static final String parseDateToStr(final String format, final Date date)
    {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 字符串 → Date（指定格式解析，解析失败抛RuntimeException）
     * 
     * @param format 日期格式
     * @param ts 日期字符串
     * @return Date对象
     */
    public static final Date dateTime(final String format, final String ts)
    {
        try
        {
            return new SimpleDateFormat(format).parse(ts);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     * 【若依高频用法】文件上传时按日期分目录存储，如 /upload/2024/01/15/xxx.jpg
     */
    public static final String datePath()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式（智能解析，自动尝试12种常见格式）
     * 【使用场景】不确定前端传来的日期字符串格式时，用此方法自动尝试解析
     * 
     * @param str 日期字符串（支持多种格式）
     * @return 解析成功的Date对象，解析失败返回null（不抛异常）
     */
    public static Date parseDate(Object str)
    {
        if (str == null)
        {
            return null;
        }
        try
        {
            // 调用父类（Apache Commons）的parseDate，传入多格式数组自动匹配
            return parseDate(str.toString(), parsePatterns);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     * 【使用场景】系统监控页面展示服务运行时长
     */
    public static Date getServerStartDate()
    {
        // 通过JMX获取JVM启动时间
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     * 
     * @param date1 日期1
     * @param date2 日期2
     * @return 相差的天数（绝对值）
     */
    public static int differentDaysByMillisecond(Date date1, Date date2)
    {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算时间差
     * 【使用场景】系统监控页面展示"服务器已运行X天X小时X分钟"
     *
     * @param endDate 最后时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    public static String timeDistance(Date endDate, Date startTime)
    {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 增加 LocalDateTime ==> Date（Java8新时间API转旧Date的桥接方法）
     * 
     * @param temporalAccessor LocalDateTime对象
     * @return Date对象
     */
    public static Date toDate(LocalDateTime temporalAccessor)
    {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     * 
     * @param temporalAccessor LocalDate对象
     * @return Date对象（时间部分为00:00:00）
     */
    public static Date toDate(LocalDate temporalAccessor)
    {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }
}
