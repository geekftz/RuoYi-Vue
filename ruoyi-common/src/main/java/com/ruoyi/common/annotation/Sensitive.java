package com.ruoyi.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ruoyi.common.config.serializer.SensitiveJsonSerializer;
import com.ruoyi.common.enums.DesensitizedType;

/**
 * 数据脱敏注解
 * <p>
 * 【作用】标在实体字段上，返回给前端的JSON中该字段自动打码（如手机号138****5678、身份证110***********123）。
 * 【原理】@JacksonAnnotationsInside是Jackson的组合注解标记，配合@JsonSerialize(using=SensitiveJsonSerializer.class)
 *         让本注解直接变成一个自定义序列化器：Jackson序列化该字段时调用SensitiveJsonSerializer，
 *         按desensitizedType指定的类型调DesensitizedUtil打码后输出。
 * 【使用示例】@Sensitive(desensitizedType = DesensitizedType.PHONE) 标在手机号字段上，
 *             列表接口返回的用户手机号自动变成138****5678，无需Controller手动处理。
 *
 * @author ruoyi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveJsonSerializer.class)
public @interface Sensitive
{
    DesensitizedType desensitizedType();
}
