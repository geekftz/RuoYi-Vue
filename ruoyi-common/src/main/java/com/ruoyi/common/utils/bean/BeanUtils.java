package com.ruoyi.common.utils.bean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean 工具类
 * <p>
 * 【架构位置】common/utils/bean，继承Spring的BeanUtils获得属性复制能力，并扩展了getter/setter方法获取。
 * 【核心方法】
 * - copyBeanProp(dest, src)：对象属性复制（DTO→Entity、Entity→VO转换必备）
 * - getSetterMethods/getGetterMethods：反射获取对象的setter/getter方法列表
 * 【使用场景】代码生成器生成代码时分析实体类结构；业务层DTO与Entity之间的属性拷贝。
 * 【使用示例】
 * <pre>
 * SysUser user = new SysUser();
 * BeanUtils.copyBeanProp(user, userDto);  // 把userDto的属性值拷贝到user
 * </pre>
 * 【注意】copyBeanProp内部调用的是Spring的copyProperties(src, dest)，参数顺序与本方法签名(dest, src)相反。
 * 
 * @author ruoyi
 */
public class BeanUtils extends org.springframework.beans.BeanUtils
{
    /** Bean方法名中属性名开始的下标（get/set前缀占3个字符） */
    private static final int BEAN_METHOD_PROP_INDEX = 3;

    /** * 匹配getter方法的正则表达式（get后首字母大写） */
    private static final Pattern GET_PATTERN = Pattern.compile("get(\\p{javaUpperCase}\\w*)");

    /** * 匹配setter方法的正则表达式（set后首字母大写） */
    private static final Pattern SET_PATTERN = Pattern.compile("set(\\p{javaUpperCase}\\w*)");

    /**
     * Bean属性复制工具方法。
     * 【若依高频用法】DTO/VO/Entity之间的属性拷贝，一行代码搞定字段赋值。
     * 同名同类型属性自动赋值，不同名或不同类型的属性忽略（不报错）。
     * 
     * @param dest 目标对象（被赋值的一方）
     * @param src 源对象（提供值的一方）
     */
    public static void copyBeanProp(Object dest, Object src)
    {
        try
        {
            // 调用Spring BeanUtils的copyProperties（注意Spring的参数顺序是src在前，dest在后）
            copyProperties(src, dest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 获取对象的setter方法（反射获取，供代码生成器等场景使用）。
     * 
     * @param obj 对象
     * @return 对象的setter方法列表
     */
    public static List<Method> getSetterMethods(Object obj)
    {
        // setter方法列表
        List<Method> setterMethods = new ArrayList<Method>();

        // 获取所有方法
        Method[] methods = obj.getClass().getMethods();

        // 查找setter方法

        for (Method method : methods)
        {
            // 正则匹配setXxx格式，且参数恰好为1个（setter规范）
            Matcher m = SET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 1))
            {
                setterMethods.add(method);
            }
        }
        // 返回setter方法列表
        return setterMethods;
    }

    /**
     * 获取对象的getter方法（反射获取，供代码生成器等场景使用）。
     * 
     * @param obj 对象
     * @return 对象的getter方法列表
     */

    public static List<Method> getGetterMethods(Object obj)
    {
        // getter方法列表
        List<Method> getterMethods = new ArrayList<Method>();
        // 获取所有方法
        Method[] methods = obj.getClass().getMethods();
        // 查找getter方法
        for (Method method : methods)
        {
            // 正则匹配getXxx格式，且无参数（getter规范）
            Matcher m = GET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 0))
            {
                getterMethods.add(method);
            }
        }
        // 返回getter方法列表
        return getterMethods;
    }

    /**
     * 检查Bean方法名中的属性名是否相等。<br>
     * 如getName()和setName()属性名一样，getName()和setAge()属性名不一样。
     * 【原理】截取方法名第4个字符开始的部分（去掉get/set前缀）比较。
     * 
     * @param m1 方法名1
     * @param m2 方法名2
     * @return 属性名一样返回true，否则返回false
     */

    public static boolean isMethodPropEquals(String m1, String m2)
    {
        return m1.substring(BEAN_METHOD_PROP_INDEX).equals(m2.substring(BEAN_METHOD_PROP_INDEX));
    }
}
