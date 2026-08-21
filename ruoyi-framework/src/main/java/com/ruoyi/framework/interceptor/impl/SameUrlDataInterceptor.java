package com.ruoyi.framework.interceptor.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.filter.RepeatedlyRequestWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpHelper;
import com.ruoyi.framework.interceptor.RepeatSubmitInterceptor;

/**
 * 判断请求url和数据是否和上一次相同，
 * 如果和上次相同，则是重复提交表单。 有效时间为10秒内。
 * <p>
 * 【架构位置】ruoyi-framework / interceptor层 —— RepeatSubmitInterceptor 的唯一实现类，基于Redis判定重复。
 * 【判定逻辑】缓存key = repeat_submit: + 请求URL + 请求头Token（区分用户）；
 * 对比两次提交的"参数内容"和"时间间隔"：参数完全一致 且 间隔小于注解指定毫秒数 → 判定重复提交。
 * 【为什么是Redis】分布式部署下多台服务器共享判定结果，单机内存Map在多实例下会失效。
 * 
 * @author ruoyi
 */
@Component
public class SameUrlDataInterceptor extends RepeatSubmitInterceptor
{
    /** 缓存中存请求参数的键名 */
    public final String REPEAT_PARAMS = "repeatParams";

    /** 缓存中存提交时间戳的键名 */
    public final String REPEAT_TIME = "repeatTime";

    // 令牌自定义标识：请求头中Token的键名（默认 Authorization），用于区分不同用户的提交
    @Value("${token.header}")
    private String header;

    /** 若依封装的Redis操作工具 */
    @Autowired
    private RedisCache redisCache;

    /**
     * 重复提交判定的具体实现
     *
     * @param request 当前请求
     * @param annotation 方法上的 @RepeatSubmit 注解（interval=判定间隔毫秒数）
     * @return true=重复提交（拦截）；false=首次/正常提交（放行）
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation)
    {
        String nowParams = "";
        // 请求被 RepeatedlyRequestWrapper 包装过（可重复读取body），读取JSON请求体作为参数指纹
        if (request instanceof RepeatedlyRequestWrapper)
        {
            RepeatedlyRequestWrapper repeatedlyRequest = (RepeatedlyRequestWrapper) request;
            nowParams = HttpHelper.getBodyString(repeatedlyRequest);
        }

        // body参数为空，获取Parameter的数据
        // GET/表单提交走这里：把Query参数序列化成JSON作为参数指纹
        if (StringUtils.isEmpty(nowParams))
        {
            nowParams = JSON.toJSONString(request.getParameterMap());
        }
        Map<String, Object> nowDataMap = new HashMap<String, Object>();
        nowDataMap.put(REPEAT_PARAMS, nowParams);
        nowDataMap.put(REPEAT_TIME, System.currentTimeMillis());

        // 请求地址（作为存放cache的key值）
        String url = request.getRequestURI();

        // 唯一值（没有消息头则使用请求地址）
        // 取请求头中的Token，保证同一URL不同用户互不干扰
        String submitKey = StringUtils.trimToEmpty(request.getHeader(header));

        // 唯一标识（指定key + url + 消息头）
        String cacheRepeatKey = CacheConstants.REPEAT_SUBMIT_KEY + url + submitKey;

        // 查Redis：该用户该URL上一次提交的参数指纹
        Object sessionObj = redisCache.getCacheObject(cacheRepeatKey);
        if (sessionObj != null)
        {
            Map<String, Object> sessionMap = (Map<String, Object>) sessionObj;
            if (sessionMap.containsKey(url))
            {
                Map<String, Object> preDataMap = (Map<String, Object>) sessionMap.get(url);
                // 参数相同 且 间隔在注解指定毫秒内 → 重复提交
                if (compareParams(nowDataMap, preDataMap) && compareTime(nowDataMap, preDataMap, annotation.interval()))
                {
                    return true;
                }
            }
        }
        // 非重复：把本次参数指纹写入Redis（过期时间=注解interval），供下一次提交对比
        Map<String, Object> cacheMap = new HashMap<String, Object>();
        cacheMap.put(url, nowDataMap);
        redisCache.setCacheObject(cacheRepeatKey, cacheMap, annotation.interval(), TimeUnit.MILLISECONDS);
        return false;
    }

    /**
     * 判断参数是否相同
     */
    private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap)
    {
        String nowParams = (String) nowMap.get(REPEAT_PARAMS);
        String preParams = (String) preMap.get(REPEAT_PARAMS);
        return nowParams.equals(preParams);
    }

    /**
     * 判断两次间隔时间：小于注解interval则视为同一次提交的重复
     */
    private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap, int interval)
    {
        long time1 = (Long) nowMap.get(REPEAT_TIME);
        long time2 = (Long) preMap.get(REPEAT_TIME);
        if ((time1 - time2) < interval)
        {
            return true;
        }
        return false;
    }
}
