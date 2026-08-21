package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 匿名访问不鉴权注解
 * <p>
 * 【作用】标在Controller方法或类上，该接口无需登录即可访问（绕过Spring Security的anyRequest().authenticated()拦截）。
 * 【处理链路】启动时PermitAllUrlProperties扫描所有@GetMapping等注解中带@Anonymous的方法 → 收集URL列表 →
 *             SecurityConfig中配置这些URL为permitAll()。
 * 【使用场景】对外开放的接口：如第三方回调、健康检查、开放API。例：@Anonymous @GetMapping("/open/api")
 * 【注意】别把涉及敏感数据的接口标上它，放行后无任何身份校验。
 * 
 * @author ruoyi
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Anonymous
{
}
