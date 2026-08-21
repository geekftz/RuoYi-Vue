package com.ruoyi.common.utils.uuid;

/**
 * ID生成器工具类
 * <p>
 * 【架构位置】common/utils/uuid，全系统唯一ID生成的统一入口。
 * 【使用场景】验证码UUID（CaptchaController）、Token标识、异步任务ID等需要唯一标识的场景。
 * 【四个方法的区别】
 * - randomUUID()：标准UUID，带横线，36字符，安全性高（SecureRandom）
 * - simpleUUID()：去掉横线的UUID，32字符，安全性高
 * - fastUUID()：性能更好的UUID（ThreadLocalRandom），36字符
 * - fastSimpleUUID()：性能更好+去横线，32字符
 * 【日常开发推荐】一般场景用fastSimpleUUID()（性能好），安全敏感场景用randomUUID()。
 * 
 * @author ruoyi
 */
public class IdUtils
{
    /**
     * 获取随机UUID
     * 
     * @return 随机UUID
     */
    public static String randomUUID()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线
     * 
     * @return 简化的UUID，去掉了横线
     */
    public static String simpleUUID()
    {
        return UUID.randomUUID().toString(true);
    }

    /**
     * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     * 
     * @return 随机UUID
     */
    public static String fastUUID()
    {
        return UUID.fastUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     * 
     * @return 简化的UUID，去掉了横线
     */
    public static String fastSimpleUUID()
    {
        return UUID.fastUUID().toString(true);
    }
}
