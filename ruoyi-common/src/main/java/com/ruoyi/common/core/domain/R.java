package com.ruoyi.common.core.domain;

import java.io.Serializable;
import com.ruoyi.common.constant.HttpStatus;

/**
 * 响应信息主体
 * <p>
 * 【架构位置】common模块 → core/domain，统一返回体的"泛型版"，与AjaxResult并列的两种返回风格之一。
 * 【与AjaxResult对比】R<T>用泛型字段{code,msg,data}，类型安全、结构固定；
 *             AjaxResult继承HashMap可随意put额外字段，更灵活。RuoYi-Vue单体版Controller主要用AjaxResult，
 *             R多见于RuoYi-Cloud微服务版Feign远程调用，本项目保留以备扩展。
 * 【返回格式】{"code":200,"msg":"操作成功","data":{...}}
 * 【前端联动】前端axios响应拦截器只看code字段：200成功、401未授权跳登录、500/601弹错误提示。
 *
 * @author ruoyi
 */
public class R<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 成功（=200，与HTTP状态码一致，前端拦截器据此判断成功） */
    public static final int SUCCESS = HttpStatus.SUCCESS;

    /** 失败（=500，前端拦截器据此弹错误提示） */
    public static final int FAIL = HttpStatus.ERROR;

    private int code;

    private String msg;

    private T data;

    public static <T> R<T> ok()
    {
        return restResult(null, SUCCESS, "操作成功");
    }

    public static <T> R<T> ok(T data)
    {
        return restResult(data, SUCCESS, "操作成功");
    }

    public static <T> R<T> ok(T data, String msg)
    {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> R<T> fail()
    {
        return restResult(null, FAIL, "操作失败");
    }

    public static <T> R<T> fail(String msg)
    {
        return restResult(null, FAIL, msg);
    }

    public static <T> R<T> fail(T data)
    {
        return restResult(data, FAIL, "操作失败");
    }

    public static <T> R<T> fail(T data, String msg)
    {
        return restResult(data, FAIL, msg);
    }

    public static <T> R<T> fail(int code, String msg)
    {
        return restResult(null, code, msg);
    }

    /**
     * 构建返回对象的私有总方法，上面所有ok()/fail()重载最终都汇聚到这里
     * <p>
     * 【设计模式】静态工厂方法：私有构造细节，对外暴露语义化的ok()/fail()，调用方无需new。
     */
    private static <T> R<T> restResult(T data, int code, String msg)
    {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }

    public static <T> Boolean isError(R<T> ret)
    {
        return !isSuccess(ret);
    }

    /** 判断远程调用/接口返回是否成功（微服务间Feign调用后常用此方法判结果） */
    public static <T> Boolean isSuccess(R<T> ret)
    {
        return R.SUCCESS == ret.getCode();
    }
}
