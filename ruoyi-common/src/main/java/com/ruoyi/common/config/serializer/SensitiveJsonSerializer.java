package com.ruoyi.common.config.serializer;

import java.io.IOException;
import java.util.Objects;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.ruoyi.common.annotation.Sensitive;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.DesensitizedType;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 数据脱敏序列化过滤
 * <p>
 * 【架构位置】common模块 → config/serializer，@Sensitive注解的执行者（Jackson自定义序列化器）。
 * 【调用链路】Controller返回实体 → Jackson序列化JSON → 字段标了@Sensitive → 进入本类serialize() →
 *             判断当前登录人是否超管（超管看明文）→ 非超管调DesensitizedUtil打码后输出。
 * 【原理】实现ContextualSerializer接口：Jackson每个字段单独调用createContextual()读取字段上的@Sensitive注解，
 *         拿到脱敏类型（手机号/身份证/银行卡等），生成字段专属的序列化器实例。
 *
 * @author ruoyi
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer
{
    private DesensitizedType desensitizedType;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        // 【核心逻辑】判断需要脱敏则用Function式脱敏器打码（desensitizer()返回的是函数接口，apply执行打码）；否则原样输出
        if (desensitization())
        {
            gen.writeString(desensitizedType.desensitizer().apply(value));
        }
        else
        {
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException
    {
        // Jackson回调：读取字段上的@Sensitive注解，只有String类型字段才启用脱敏（手机号等脱敏对象都是字符串）
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass()))
        {
            this.desensitizedType = annotation.desensitizedType();
            return this;
        }
        // 字段没标@Sensitive或不是String类型，走Jackson默认序列化器
        return prov.findValueSerializer(property.getType(), property);
    }

    /**
     * 是否需要脱敏处理
     * <p>
     * 【特权设计】超管admin不脱敏（方便管理员排查问题看完整数据）；
     * 未登录/获取登录信息异常时默认脱敏（宁枉勿纵的安全默认策略）。
     */
    private boolean desensitization()
    {
        try
        {
            LoginUser securityUser = SecurityUtils.getLoginUser();
            // 管理员不脱敏
            return !securityUser.getUser().isAdmin();
        }
        catch (Exception e)
        {
            return true;
        }
    }
}
