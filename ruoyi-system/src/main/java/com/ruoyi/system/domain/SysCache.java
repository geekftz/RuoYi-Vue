package com.ruoyi.system.domain;

import com.ruoyi.common.utils.StringUtils;

/**
 * 缓存信息
 * <p>
 * 【架构位置】ruoyi-system → domain，注意这不是数据库实体！是「缓存监控」页面的展示对象（VO）。
 * 【使用场景】系统监控 → 缓存监控菜单：后端从 Redis 读取 key 列表/键名/键值，包装成 SysCache 返回前端展示，
 * 对应 CacheController（如 getCacheList/getCacheKey/getCacheValue 接口）。
 * 【key 结构】若依 Redis key 约定「前缀:业务id」形式（如 login_tokens:uuid、sys_dict:sys_user_sex），
 * 前缀在 CacheConstants 中统一定义。
 * 
 * @author ruoyi
 */
public class SysCache
{
    /** 缓存名称：key 的前缀部分，如 login_tokens、sys_dict */
    private String cacheName = "";

    /** 缓存键名：完整 key 去掉前缀后的业务标识部分 */
    private String cacheKey = "";

    /** 缓存内容：Redis 中存储的序列化值 */
    private String cacheValue = "";

    /** 备注 */
    private String remark = "";

    public SysCache()
    {

    }

    public SysCache(String cacheName, String remark)
    {
        this.cacheName = cacheName;
        this.remark = remark;
    }

    public SysCache(String cacheName, String cacheKey, String cacheValue)
    {
        // 把完整 Redis key 拆成「前缀」和「业务键」两部分：如 "sys_dict:sys_user_sex" → 名称sys_dict + 键名sys_user_sex
        this.cacheName = StringUtils.replace(cacheName, ":", "");
        this.cacheKey = StringUtils.replace(cacheKey, cacheName, "");
        this.cacheValue = cacheValue;
    }

    public String getCacheName()
    {
        return cacheName;
    }

    public void setCacheName(String cacheName)
    {
        this.cacheName = cacheName;
    }

    public String getCacheKey()
    {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey)
    {
        this.cacheKey = cacheKey;
    }

    public String getCacheValue()
    {
        return cacheValue;
    }

    public void setCacheValue(String cacheValue)
    {
        this.cacheValue = cacheValue;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
