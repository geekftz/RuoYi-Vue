package com.ruoyi.common.filter;

import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;

/**
 * 排除JSON敏感属性
 * <p>
 * 【架构位置】common/filter，FastJson2序列化过滤器（不是Servlet过滤器，与XssFilter等不是一类东西）。
 * 【能力】序列化对象为JSON字符串时，排除指定的属性字段，防止敏感数据（如密码）泄露到响应体中。
 * 【使用场景】某些场景需要手动用FastJson2序列化对象返回时，先调用addExcludes("password")排除敏感字段。
 * 【使用示例】
 * <pre>
 * PropertyPreExcludeFilter filter = new PropertyPreExcludeFilter().addExcludes("password", "salt");
 * String json = JSON.toJSONString(user, filter); // 输出json中不包含password/salt字段
 * </pre>
 * 【说明】若依日常开发中主要通过@JsonProperty(WRITE_ONLY)或@JSONField(serialize=false)注解控制序列化，
 * 本类作为编程式补充手段，适合无法修改实体类的场景。
 * 
 * @author ruoyi
 */
public class PropertyPreExcludeFilter extends SimplePropertyPreFilter
{
    public PropertyPreExcludeFilter()
    {
    }

    /**
     * 添加要排除的属性名（可变参数，可传多个），返回this支持链式调用
     * 
     * @param filters 要排除的字段名，如"password"、"salt"
     * @return this（链式编程）
     */
    public PropertyPreExcludeFilter addExcludes(String... filters)
    {
        for (int i = 0; i < filters.length; i++)
        {
            // SimplePropertyPreFilter内置的getExcludes()集合，加入排除字段名
            this.getExcludes().add(filters[i]);
        }
        return this;
    }
}
