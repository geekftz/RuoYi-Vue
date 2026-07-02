package com.ruoyi.framework.aspectj;

import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessStatus;
import com.ruoyi.common.enums.HttpMethod;
import com.ruoyi.common.filter.PropertyPreExcludeFilter;
import com.ruoyi.common.utils.ExceptionUtil;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.domain.SysOperLog;

/**
 * 操作日志记录切面
 * <p>
 * 通过 AOP 拦截带有 @Log 注解的 Controller 方法，自动记录操作日志到 sys_oper_log 表。
 * </p>
 * <p>
 * 记录流程：
 * 1. @Before：方法执行前记录开始时间（存入ThreadLocal）
 * 2. @AfterReturning：方法正常返回后，异步保存日志
 * 3. @AfterThrowing：方法抛出异常后，异步保存日志（含异常信息）
 * </p>
 * <p>
 * 记录内容：操作人、IP、请求URL、请求方式、方法名、业务类型、操作参数、
 * 返回结果、消耗时间、异常信息等。敏感字段（password等）会被自动过滤。
 * </p>
 *
 * @author ruoyi
 */
@Aspect
@Component
public class LogAspect
{
    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /** 排除敏感属性字段 */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

    /** 计算操作消耗时间 */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<Long>("Cost Time");

    /** 参数最大长度限制 */
    private static final int PARAM_MAX_LENGTH = 2000;

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, Log controllerLog)
    {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult)
    {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     * 
     * @param joinPoint 切点
     * @param e 异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e)
    {
        handleLog(joinPoint, controllerLog, e, null);
    }

    /**
     * 日志记录核心处理方法
     * <p>
     * 构建SysOperLog对象，填充操作人、IP、URL、方法名、参数、返回值、消耗时间等信息，
     * 通过异步任务保存到 sys_oper_log 表。
     * </p>
     *
     * @param joinPoint   切点
     * @param controllerLog 日志注解
     * @param e           异常对象（正常返回时为null）
     * @param jsonResult  方法返回值
     */
    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult)
    {
        try
        {
            // 获取当前登录用户
            LoginUser loginUser = SecurityUtils.getLoginUser();

            // 构建操作日志对象
            SysOperLog operLog = new SysOperLog();
            // 默认状态为成功
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 获取客户端IP地址
            String ip = IpUtils.getIpAddr();
            operLog.setOperIp(ip);
            // 设置请求URL（截断为255字符防止超长）
            operLog.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            if (loginUser != null)
            {
                // 设置操作人用户名
                operLog.setOperName(loginUser.getUsername());
                SysUser currentUser = loginUser.getUser();
                if (StringUtils.isNotNull(currentUser) && StringUtils.isNotNull(currentUser.getDept()))
                {
                    // 设置操作人部门名称
                    operLog.setDeptName(currentUser.getDept().getDeptName());
                }
            }

            // 如果有异常，设置状态为失败并记录错误信息
            if (e != null)
            {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(Convert.toStr(e.getMessage(), ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            }
            // 设置被调用的方法全限定名（类名.方法名()）
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 设置HTTP请求方式（GET/POST/PUT/DELETE）
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 从注解中解析业务类型、标题、操作人类型等描述信息
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 计算并设置操作消耗时间（当前时间 - 方法开始时间）
            operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
            // 异步保存日志到数据库，不影响主流程性能
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        }
        catch (Exception exp)
        {
            // 日志记录本身出错时，记录本地日志但不影响业务流程
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
        finally
        {
            // 清理ThreadLocal，防止内存泄漏
            TIME_THREADLOCAL.remove();
        }
    }

    /**
     * 从 @Log 注解中解析方法描述信息并设置到操作日志对象
     * <p>
     * 解析内容包括：业务类型（增删改查等）、模块标题、操作人类别、
     * 请求参数（可选）、返回结果（可选）。
     * </p>
     *
     * @param joinPoint  切点
     * @param log        日志注解
     * @param operLog    操作日志对象
     * @param jsonResult 方法返回值
     * @throws Exception 异常
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult) throws Exception
    {
        // 设置业务操作类型（OTHER/INSERT/UPDATE/DELETE/GRANT/EXPORT/IMPORT/FORCE/GENCODE/CLEAN）
        operLog.setBusinessType(log.businessType().ordinal());
        // 设置模块标题（如"用户管理"）
        operLog.setTitle(log.title());
        // 设置操作人类别（OTHER/WEB/MOBILE）
        operLog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存请求参数
        if (log.isSaveRequestData())
        {
            // 获取请求参数并设置到日志中
            setRequestValue(joinPoint, operLog, log.excludeParamNames());
        }
        // 是否需要保存返回结果
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult))
        {
            // 将返回结果转为JSON字符串（截断为2000字符）
            operLog.setJsonResult(StringUtils.substring(JSON.toJSONString(jsonResult), 0, 2000));
        }
    }

    /**
     * 获取请求参数并设置到操作日志中
     * <p>
     * 对于 PUT/POST/DELETE 请求，参数在请求体中，通过反射获取方法参数；
     * 对于 GET 请求，参数在 URL 中，直接从请求参数Map获取。
     * </p>
     *
     * @param joinPoint         切点
     * @param operLog           操作日志对象
     * @param excludeParamNames 需要排除的参数名（敏感参数）
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog, String[] excludeParamNames) throws Exception
    {
        String requestMethod = operLog.getRequestMethod();
        // 获取请求参数Map
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        if (StringUtils.isEmpty(paramsMap) && StringUtils.equalsAny(requestMethod, HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name()))
        {
            // PUT/POST/DELETE请求体参数：通过反射获取方法参数数组并转为JSON
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            operLog.setOperParam(params);
        }
        else
        {
            // GET请求参数：直接将参数Map转为JSON（过滤敏感字段，截断为2000字符）
            operLog.setOperParam(StringUtils.substring(JSON.toJSONString(paramsMap, excludePropertyPreFilter(excludeParamNames)), 0, PARAM_MAX_LENGTH));
        }
    }

    /**
     * 将方法参数数组拼接为 JSON 字符串
     * <p>
     * 遍历参数数组，过滤掉 MultipartFile、HttpServletRequest 等非业务对象，
     * 将剩余参数转为 JSON 并拼接，超过2000字符时截断。
     * </p>
     *
     * @param paramsArray       参数数组
     * @param excludeParamNames 需要排除的参数名
     * @return 拼接后的JSON字符串
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames)
    {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0)
        {
            for (Object o : paramsArray)
            {
                if (StringUtils.isNotNull(o) && !isFilterObject(o))
                {
                    try
                    {
                        String jsonObj = JSON.toJSONString(o, excludePropertyPreFilter(excludeParamNames));
                        params.append(jsonObj).append(" ");
                        if (params.length() >= PARAM_MAX_LENGTH)
                        {
                            return StringUtils.substring(params.toString(), 0, PARAM_MAX_LENGTH);
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("请求参数拼装异常 msg:{}, 参数:{}", e.getMessage(), paramsArray, e);
                    }
                }
            }
        }
        return params.toString();
    }

    /**
     * 创建敏感属性过滤器
     * <p>
     * 合并默认排除的敏感字段（password等）和注解上指定的排除字段，
     * 返回一个 FastJSON 的属性过滤器，在序列化时自动忽略这些字段。
     * </p>
     *
     * @param excludeParamNames 需要排除的参数名
     * @return 属性过滤器
     */
    public PropertyPreExcludeFilter excludePropertyPreFilter(String[] excludeParamNames)
    {
        return new PropertyPreExcludeFilter().addExcludes(ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames));
    }

    /**
     * 判断是否需要过滤的对象。
     * 
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o)
    {
        Class<?> clazz = o.getClass();
        if (clazz.isArray())
        {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        }
        else if (Collection.class.isAssignableFrom(clazz))
        {
            Collection collection = (Collection) o;
            for (Object value : collection)
            {
                return value instanceof MultipartFile;
            }
        }
        else if (Map.class.isAssignableFrom(clazz))
        {
            Map map = (Map) o;
            for (Object value : map.entrySet())
            {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
