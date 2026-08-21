package com.ruoyi.common.enums;

import java.util.function.Function;
import com.ruoyi.common.utils.DesensitizedUtil;

/**
 * 脱敏类型
 * <p>
 * 【使用者】@Sensitive(desensitizedType=DesensitizedType.PHONE)注解的取值，SensitiveJsonSerializer序列化时调用。
 * 【设计亮点】每个枚举值携带一个Function<String,String>脱敏函数（策略模式的枚举实现）：
 * 枚举项直接内联正则替换逻辑或委托给DesensitizedUtil，调用方只需desensitizer().apply(value)即可完成打码，
 * 新增脱敏类型只需加一个枚举项，不改任何调用代码。
 *
 * @author ruoyi
 */
public enum DesensitizedType
{
    /**
     * 姓名，第2位星号替换
     */
    USERNAME(s -> s.replaceAll("(\\S)\\S(\\S*)", "$1*$2")),

    /**
     * 密码，全部字符都用*代替
     */
    PASSWORD(DesensitizedUtil::password),

    /**
     * 身份证，中间10位星号替换
     */
    ID_CARD(s -> s.replaceAll("(\\d{4})\\d{10}(\\d{3}[Xx]|\\d{4})", "$1** **** ****$2")),

    /**
     * 手机号，中间4位星号替换（效果：13812345678 → 138****5678，正则捕获前3位+后4位保留，中间替换）
     */
    PHONE(s -> s.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2")),

    /**
     * 电子邮箱，仅显示第一个字母和@后面的地址显示，其他星号替换（效果：zhangsan@qq.com → z****@qq.com）
     */
    EMAIL(s -> s.replaceAll("(^.)[^@]*(@.*$)", "$1****$2")),

    /**
     * 银行卡号，保留最后4位，其他星号替换（效果：6222 **** **** **** 123）
     */
    BANK_CARD(s -> s.replaceAll("\\d{15}(\\d{3})", "**** **** **** **** $1")),

    /**
     * 车牌号码，包含普通车辆、新能源车辆
     */
    CAR_LICENSE(DesensitizedUtil::carLicense);

    private final Function<String, String> desensitizer;

    DesensitizedType(Function<String, String> desensitizer)
    {
        this.desensitizer = desensitizer;
    }

    public Function<String, String> desensitizer()
    {
        return desensitizer;
    }
}
