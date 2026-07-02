package com.ruoyi.web.controller.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysUserOnline;
import com.ruoyi.system.service.ISysUserOnlineService;

/**
 * 在线用户监控控制器
 * <p>
 * 通过扫描 Redis 中的登录令牌缓存（login_tokens:*），实时展示当前在线用户列表，
 * 支持按IP和用户名筛选，以及强制踢出用户。
 * </p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController
{
    /** 在线用户业务层 */
    @Autowired
    private ISysUserOnlineService userOnlineService;

    /** Redis 缓存 */
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询在线用户列表
     * <p>
     * 扫描 Redis 中所有 login_tokens:* 的缓存key，获取所有在线用户的 LoginUser 信息。
     * 支持按 IP地址和用户名进行筛选，返回最近登录的用户排在前面。
     * </p>
     *
     * @param ipaddr  IP地址筛选条件（可选）
     * @param userName 用户名筛选条件（可选）
     * @return 在线用户分页列表
     */
    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName)
    {
        // 扫描所有在线用户的Redis缓存key
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys)
        {
            LoginUser user = redisCache.getCacheObject(key);
            // 根据筛选条件匹配用户
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName))
            {
                // 同时按IP和用户名筛选
                userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
            }
            else if (StringUtils.isNotEmpty(ipaddr))
            {
                // 仅按IP筛选
                userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
            }
            else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser()))
            {
                // 仅按用户名筛选
                userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
            }
            else
            {
                // 无筛选条件，返回所有在线用户
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }
        // 反转列表，最近登录的用户排前面
        Collections.reverse(userOnlineList);
        // 移除不匹配筛选条件的null元素
        userOnlineList.removeAll(Collections.singleton(null));
        return getDataTable(userOnlineList);
    }

    /**
     * 强制踢出用户
     * <p>
     * 删除 Redis 中该用户的登录令牌缓存，使用户的 JWT 立即失效，
     * 后续请求将无法通过认证。
     * </p>
     *
     * @param tokenId 用户令牌UUID
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId)
    {
        // 删除Redis中的用户令牌缓存，使JWT立即失效
        redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        return success();
    }
}
