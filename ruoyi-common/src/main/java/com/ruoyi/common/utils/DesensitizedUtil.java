package com.ruoyi.common.utils;

/**
 * 脱敏工具类
 * <p>
 * 【能力】对敏感数据进行脱敏处理（部分字符替换为*），防止敏感信息在界面或日志中泄露。
 * 【使用场景】展示用户列表时手机号、密码等敏感字段脱敏后返回前端。
 * 【关联注解】若依还有@Sensitive注解+Jackson序列化器实现自动脱敏（见DesensitizedType枚举），
 * 本工具类是编程式手动脱敏的补充。
 * 【使用示例】
 * <pre>
 * DesensitizedUtil.password("123456");    // "******"
 * DesensitizedUtil.carLicense("京A12345"); // "京A12***"
 * </pre>
 *
 * @author ruoyi
 */
public class DesensitizedUtil
{
    /**
     * 密码的全部字符都用*代替，比如：******
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String password(String password)
    {
        if (StringUtils.isBlank(password))
        {
            return StringUtils.EMPTY;
        }
        // repeat重复字符：生成与密码等长的*号串
        return StringUtils.repeat('*', password.length());
    }

    /**
     * 车牌中间用*代替，如果是错误的车牌，不处理
     *
     * @param carLicense 完整的车牌号
     * @return 脱敏后的车牌
     */
    public static String carLicense(String carLicense)
    {
        if (StringUtils.isBlank(carLicense))
        {
            return StringUtils.EMPTY;
        }
        // 普通车牌（7位）：隐藏第4~6位，如"京A12345"→"京A1***"
        if (carLicense.length() == 7)
        {
            // StringUtils.hide(str, start, end)：将str的[start,end)区间字符替换为*
            carLicense = StringUtils.hide(carLicense, 3, 6);
        }
        else if (carLicense.length() == 8)
        {
            // 新能源车牌（8位）：隐藏第4~7位
            carLicense = StringUtils.hide(carLicense, 3, 7);
        }
        return carLicense;
    }
}
