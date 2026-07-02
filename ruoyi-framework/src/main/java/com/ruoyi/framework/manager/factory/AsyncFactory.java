package com.ruoyi.framework.manager.factory;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.LogUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.UserAgentUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SysLogininfor;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.service.ISysLogininforService;
import com.ruoyi.system.service.ISysOperLogService;

/**
 * 异步任务工厂
 * <p>
 * 创建异步执行的任务（TimerTask），由 AsyncManager 线程池调度执行。
 * 主要用于异步记录登录日志和操作日志，避免日志写入影响主流程性能。
 * </p>
 *
 * @author ruoyi
 */
public class AsyncFactory
{
    /** 用户登录日志记录器 */
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 创建记录登录信息的异步任务
     * <p>
     * 在登录/退出/注册时调用，异步将登录日志写入 sys_logininfor 表。
     * 包含：用户名、IP、归属地、浏览器、操作系统、状态、消息等信息。
     * </p>
     * <p>
     * 注意：在创建任务时（非执行时）就要获取 User-Agent 和 IP，
     * 因为异步执行时请求上下文可能已经释放。
     * </p>
     *
     * @param username 用户名
     * @param status   状态（LOGIN_SUCCESS/LOGIN_FAIL/LOGOUT/REGISTER）
     * @param message  消息内容
     * @param args     日志格式化参数
     * @return TimerTask 异步任务
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message,
            final Object... args)
    {
        // 在当前线程获取请求信息（异步执行时请求上下文不可用）
        final String userAgent = ServletUtils.getRequest().getHeader("User-Agent");
        final String ip = IpUtils.getIpAddr();
        return new TimerTask()
        {
            @Override
            public void run()
            {
                // 根据IP查询归属地
                String address = AddressUtils.getRealAddressByIP(ip);
                // 构建日志输出内容
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印登录日志到日志文件
                sys_user_logger.info(s.toString(), args);
                // 解析客户端操作系统和浏览器
                String os = UserAgentUtils.getOperatingSystem(userAgent);
                String browser = UserAgentUtils.getBrowser(userAgent);
                // 封装登录日志对象
                SysLogininfor logininfor = new SysLogininfor();
                logininfor.setUserName(username);
                logininfor.setIpaddr(ip);
                logininfor.setLoginLocation(address);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setMsg(message);
                // 根据状态设置日志状态（成功/失败）
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
                {
                    logininfor.setStatus(Constants.SUCCESS);
                }
                else if (Constants.LOGIN_FAIL.equals(status))
                {
                    logininfor.setStatus(Constants.FAIL);
                }
                // 通过Spring上下文获取Service并异步插入数据库
                SpringUtils.getBean(ISysLogininforService.class).insertLogininfor(logininfor);
            }
        };
    }

    /**
     * 创建记录操作日志的异步任务
     * <p>
     * 在Controller方法执行完成后由LogAspect调用，异步将操作日志写入 sys_oper_log 表。
     * 包含：操作人、IP、URL、方法名、业务类型、请求参数、返回结果、消耗时间等。
     * </p>
     *
     * @param operLog 操作日志信息（由LogAspect构建）
     * @return TimerTask 异步任务
     */
    public static TimerTask recordOper(final SysOperLog operLog)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                // 根据操作IP查询归属地
                operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
                // 通过Spring上下文获取Service并异步插入数据库
                SpringUtils.getBean(ISysOperLogService.class).insertOperlog(operLog);
            }
        };
    }
}
