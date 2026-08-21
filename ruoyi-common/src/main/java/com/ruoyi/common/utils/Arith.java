package com.ruoyi.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 精确的浮点数运算
 * <p>
 * 【能力】基于BigDecimal实现加减乘除和四舍五入，解决double/float直接运算的精度丢失问题。
 * 【为什么要用它】Java中 0.1 + 0.2 ≠ 0.3（结果是0.30000000000000004），涉及金额、比率计算时必须用本工具类。
 * 【使用场景】金额计算、百分比计算、统计报表中的数值运算。
 * 【使用示例】
 * <pre>
 * double result = Arith.add(0.1, 0.2);        // 0.3（精确）
 * double avg = Arith.div(10, 3, 2);           // 3.33（保留2位小数）
 * double rounded = Arith.round(3.14159, 2);   // 3.14（四舍五入）
 * </pre>
 * 【原理】内部先将double转为字符串再构造BigDecimal（避免new BigDecimal(0.1)的精度问题），运算后转回double。
 * 
 * @author ruoyi
 */
public class Arith
{

    /** 默认除法运算精度（除不尽时保留10位小数） */
    private static final int DEF_DIV_SCALE = 10;

    /** 这个类不能实例化（纯静态工具类，私有构造防止new） */
    private Arith()
    {
    }

    /**
     * 提供精确的加法运算。
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static double mul(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1, double v2)
    {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        // 关键：用Double.toString()中转，避免new BigDecimal(0.1)时底层二进制表示导致的精度丢失
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        // 被除数为0直接返回0，避免除法运算
        if (b1.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO.doubleValue();
        }
        // RoundingMode.HALF_UP = 四舍五入
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        // 除以1实现四舍五入取整到指定小数位
        return b.divide(BigDecimal.ONE, scale, RoundingMode.HALF_UP).doubleValue();
    }
}
