package com.ruoyi.common.xss;

import com.ruoyi.common.utils.StringUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义xss校验注解实现
 * <p>
 * 【架构位置】common/xss，@Xss注解的实际校验逻辑实现类。
 * 【校验逻辑】用正则表达式检测字符串中是否包含HTML标签，包含则校验失败。
 * 【触发时机】Controller参数带@Validated → Spring校验框架自动调用本类的isValid()方法。
 * 【与EscapeUtil.clean()的区别】本类只检测不修改（发现HTML返回false校验失败），
 * EscapeUtil.clean()是清洗（把HTML标签移除后继续处理）。
 * 
 * @author ruoyi
 */
public class XssValidator implements ConstraintValidator<Xss, String>
{
    /** HTML标签正则：匹配 <xxx>内容</xxx> 或 <xxx /> 形式的标签 */
    private static final String HTML_PATTERN = "<(\\S*?)[^>]*>.*?|<.*? />";

    /**
     * JSR-303校验入口：返回true表示校验通过（不含HTML标签），false表示校验失败
     * 
     * @param value 被校验的字段值
     * @param constraintValidatorContext 校验上下文
     * @return true=安全（无HTML标签），false=包含HTML标签
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext)
    {
        // 空值放行（由@NotBlank等其他注解处理空值校验）
        if (StringUtils.isBlank(value))
        {
            return true;
        }
        // 包含HTML标签则校验失败
        return !containsHtml(value);
    }

    /**
     * 检测字符串是否包含HTML标签
     * 
     * @param value 待检测字符串
     * @return true=包含HTML标签
     */
    public static boolean containsHtml(String value)
    {
        StringBuilder sHtml = new StringBuilder();
        Pattern pattern = Pattern.compile(HTML_PATTERN);
        Matcher matcher = pattern.matcher(value);
        // 收集所有匹配到的HTML标签片段
        while (matcher.find())
        {
            sHtml.append(matcher.group());
        }
        // 对收集到的片段再次整体匹配，确认是否构成完整的HTML标签
        return pattern.matcher(sHtml).matches();
    }
}