package com.ruoyi.common.core.domain.model;

/**
 * 用户注册对象
 * <p>
 * 【架构位置】common模块 → core/domain/model，注册接口入参DTO。
 * 【设计】直接继承LoginBody复用username/password/code/uuid四个字段——注册和登录需要的参数完全一致。
 * 【调用链路】前端注册页提交 → POST /register → SysRegisterController.register(@RequestBody RegisterBody)
 *             → SysRegisterService.register()（校验验证码 → 查重 → BCrypt加密 → 落库）。
 * 【扩展提示】若注册页要收集邮箱/手机号等更多字段，直接在本类追加字段即可，不影响登录逻辑。
 * 
 * @author ruoyi
 */
public class RegisterBody extends LoginBody
{

}
