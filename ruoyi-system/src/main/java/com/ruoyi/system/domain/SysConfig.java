package com.ruoyi.system.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 参数配置表 sys_config
 * <p>
 * 【架构位置】ruoyi-system → domain，对应数据库表 sys_config（系统参数配置）。
 * 【业务作用】「参数设置」菜单的数据载体：以 key-value 形式存系统级开关/配置，
 * 例如 sys.user.registerEnabled（是否允许注册）、sys.index.skinName（主题皮肤）等。
 * 【使用方式】后端用 SysConfigService.selectConfigByKey("key") 读取；读出来的值会缓存进 Redis（前缀 sys_config:）。
 * 【与字典的区别】sys_config 管「系统开关类单值」；sys_dict 管「枚举选项类多值」。
 * 【基类】继承 BaseEntity，自带 createBy/createTime/updateBy/updateTime/remark 审计字段。
 * 
 * @author ruoyi
 */
public class SysConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 参数主键 */
    @Excel(name = "参数主键", cellType = ColumnType.NUMERIC)
    private Long configId;

    /** 参数名称：给人看的描述，如「用户管理-是否允许注册」 */
    @Excel(name = "参数名称")
    private String configName;

    /** 参数键名：代码中读取用的 key，约定「模块.功能.配置项」三段式，如 sys.user.registerEnabled */
    @Excel(name = "参数键名")
    private String configKey;

    /** 参数键值：统一用 String 存储，代码里按业务需要转成 boolean/int 等 */
    @Excel(name = "参数键值")
    private String configValue;

    /** 系统内置（Y是 N否）：Y 的记录禁止删除/修改键名，防止误删系统必需的配置 */
    @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
    private String configType;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    @NotBlank(message = "参数名称不能为空")
    @Size(min = 0, max = 100, message = "参数名称不能超过100个字符")
    public String getConfigName()
    {
        return configName;
    }

    public void setConfigName(String configName)
    {
        this.configName = configName;
    }

    @NotBlank(message = "参数键名长度不能为空")
    @Size(min = 0, max = 100, message = "参数键名长度不能超过100个字符")
    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }

    @NotBlank(message = "参数键值不能为空")
    @Size(min = 0, max = 500, message = "参数键值长度不能超过500个字符")
    public String getConfigValue()
    {
        return configValue;
    }

    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }

    public String getConfigType()
    {
        return configType;
    }

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configName", getConfigName())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("configType", getConfigType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
