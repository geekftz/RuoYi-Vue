package com.ruoyi.framework.config;

import java.util.Random;
import com.google.code.kaptcha.text.impl.DefaultTextCreator;

/**
 * 验证码文本生成器
 * <p>
 * 【职责】为"数学计算型"验证码生成题目文本，替代Kaptcha默认的随机字符。
 * 在 CaptchaConfig 的 captchaProducerMath 中通过 KAPTCHA_TEXTPRODUCER_IMPL 指定本类生效。
 * <p>
 * 【输出格式巧思】返回字符串形如 "8-3=?@5"：
 * - "@" 前半部分 "8-3=?" 是要画到图片上给用户看的算式；
 * - "@" 后半部分 "5" 是正确答案，CaptchaController 生成图片后用 "@" 切分，把答案存Redis等待用户提交比对。
 *
 * @author ruoyi
 */
public class KaptchaTextCreator extends DefaultTextCreator
{
    /** 操作数取值表：0~10（乘法结果最大10*10=100以内，保证口算难度适中） */
    private static final String[] CNUMBERS = "0,1,2,3,4,5,6,7,8,9,10".split(",");

    /**
     * 生成一道随机四则运算题（乘 / 除或加 / 减），并保证结果为整数且非负
     *
     * @return 格式 "算式=?@答案"，如 "6/2=?@3"
     */
    @Override
    public String getText()
    {
        Integer result = 0;
        Random random = new Random();
        // 随机生成两个 0~9 的操作数
        int x = random.nextInt(10);
        int y = random.nextInt(10);
        StringBuilder suChinese = new StringBuilder();
        // 随机选择运算符：0=乘法，1=除法(除不尽则降级为加法)，2=减法(保证非负)
        int randomoperands = random.nextInt(3);
        if (randomoperands == 0)
        {
            result = x * y;
            suChinese.append(CNUMBERS[x]);
            suChinese.append("*");
            suChinese.append(CNUMBERS[y]);
        }
        else if (randomoperands == 1)
        {
            if ((x != 0) && y % x == 0)
            {
                result = y / x;
                suChinese.append(CNUMBERS[y]);
                suChinese.append("/");
                suChinese.append(CNUMBERS[x]);
            }
            else
            {
                result = x + y;
                suChinese.append(CNUMBERS[x]);
                suChinese.append("+");
                suChinese.append(CNUMBERS[y]);
            }
        }
        else
        {
            // 减法：始终用大数减小数，保证结果非负
            if (x >= y)
            {
                result = x - y;
                suChinese.append(CNUMBERS[x]);
                suChinese.append("-");
                suChinese.append(CNUMBERS[y]);
            }
            else
            {
                result = y - x;
                suChinese.append(CNUMBERS[y]);
                suChinese.append("-");
                suChinese.append(CNUMBERS[x]);
            }
        }
        // 拼接 "=?@答案"：图片上显示算式，@后的答案留给后端校验用
        suChinese.append("=?@" + result);
        return suChinese.toString();
    }
}