package com.ruoyi.system.service.impl;

import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 参数配置服务实现
 * <p>
 * 管理系统参数配置（sys_config表），支持 Redis 缓存。
 * 参数查询优先从 Redis 缓存获取，不存在则查数据库并写入缓存。
 * </p>
 * 【架构位置】业务层 ServiceImpl，对应 SysConfigController。
 * 【核心设计】参数是典型的"读多写少"数据，若依用 Redis 做缓存：
 * 查询走"缓存→数据库→回填缓存"三段式；增删改时同步维护缓存，保证一致性。
 * 【缓存key规则】CacheConstants.SYS_CONFIG_KEY("sys_config:") + configKey，如 sys_config:sys.account.captchaEnabled
 *
 * @author ruoyi
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService
{
    /** 参数配置 Mapper */
    @Autowired
    private SysConfigMapper configMapper;

    /** Redis 缓存 */
    @Autowired
    private RedisCache redisCache;

    /**
     * 项目启动时，初始化参数到缓存
     * 【注解专项】@PostConstruct：Spring Bean 初始化完成后自动执行一次，
     * 保证应用启动后参数缓存立即可用，避免首次请求穿透到数据库。
     */
    @PostConstruct
    public void init()
    {
        // 全量加载 sys_config 表到 Redis
        loadingConfigCache();
    }

    /**
     * 查询参数配置信息
     * 
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfig selectConfigById(Long configId)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息
     * 
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey)
    {
        // ① 先查 Redis 缓存（Convert.toStr 把缓存对象安全转字符串，null→""）
        String configValue = Convert.toStr(redisCache.getCacheObject(getCacheKey(configKey)));
        if (StringUtils.isNotEmpty(configValue))
        {
            // 缓存命中，直接返回，不查库
            return configValue;
        }
        // ② 缓存未命中，查数据库
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig))
        {
            // ③ 查到后回填缓存，下次请求直接命中
            redisCache.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }
        // 数据库也没有，返回空串（StringUtils.EMPTY = ""）
        return StringUtils.EMPTY;
    }

    /**
     * 获取验证码开关
     * 
     * @return true开启，false关闭
     */
    @Override
    public boolean selectCaptchaEnabled()
    {
        // 读取参数 sys.account.captchaEnabled（系统管理→参数设置可配置）
        // 【调用链路】登录接口 SysLoginService.login() 第一步就调此方法决定是否校验验证码
        String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
        if (StringUtils.isEmpty(captchaEnabled))
        {
            // 参数不存在时默认开启验证码（安全兜底）
            return true;
        }
        // Convert.toBool：把 "true"/"false" 字符串转 boolean
        return Convert.toBool(captchaEnabled);
    }

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config)
    {
        return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfig config)
    {
        int row = configMapper.insertConfig(config);
        if (row > 0)
        {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfig config)
    {
        // 先查出旧记录：如果修改了 configKey，需要把旧 key 的缓存删掉，否则会残留脏数据
        SysConfig temp = configMapper.selectConfigById(config.getConfigId());
        if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey()))
        {
            // key 变了 → 删除旧 key 对应的缓存
            redisCache.deleteObject(getCacheKey(temp.getConfigKey()));
        }

        int row = configMapper.updateConfig(config);
        if (row > 0)
        {
            // 更新成功 → 刷新新 key 的缓存
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            // 内置参数（configType=Y）是系统运行必需，禁止删除，抛业务异常由全局异常处理器返回前端
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            // Mapper 删除数据库记录
            configMapper.deleteConfigById(configId);
            // 同步清除缓存，避免删了库里还命中缓存
            redisCache.deleteObject(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache()
    {
        // 无条件查询全表，逐条写入 Redis
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList)
        {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    @Override
    public void clearConfigCache()
    {
        // redisCache.keys 用模式匹配批量找出所有 sys_config:* 的 key，再批量删除
        Collection<String> keys = redisCache.keys(CacheConstants.SYS_CONFIG_KEY + "*");
        redisCache.deleteObject(keys);
    }

    /**
     * 重置参数缓存数据
     */
    @Override
    public void resetConfigCache()
    {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfig config)
    {
        // 唯一性校验套路：新增时 configId 为 null，用 -1L 占位；修改时排除自身
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        // 查到了同名记录且不是当前这条 → 不唯一
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     * 
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey)
    {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }
}
