package com.ruoyi.common.core.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Entity基类（若依核心基类 · 重点掌握）
 * <p>
 * 【架构位置】ruoyi-common / core / domain层 —— 所有业务实体（SysUser、SysRole、SysDept、SysNotice...）的父类。
 * 【自带字段与数据库对应】本类字段对应每张业务表的公共列：
 *   create_by/create_time/update_by/update_time/remark —— 五张审计列，每张若依业务表都有。
 * <p>
 * 【填充逻辑】不是数据库自动填充，而是代码手动赋值，惯例：
 *   新增：entity.setCreateBy(SecurityUtils.getUsername()) + createTime在XML的insert语句里写 sysdate()；
 *   修改：entity.setUpdateBy(SecurityUtils.getUsername()) + updateTime在XML的update语句里写 sysdate()。
 * <p>
 * 【两个特殊字段】
 *   searchValue —— 页面顶部搜索框的模糊关键字，XML中用于拼接 OR 模糊查询条件；
 *   params      —— 扩展请求参数Map，最典型的用法是前端传 beginTime/endTime 做时间范围查询
 *                 （XML中写 params.beginTime 取值），以及数据权限注入的 dataScope SQL片段。
 *
 * @author ruoyi
 */
public class BaseEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 搜索值（页面通用搜索框关键字；@JsonIgnore 表示返回前端时序列化忽略此字段） */
    @JsonIgnore
    private String searchValue;

    /** 创建者（存登录用户名 user_name，由代码填充非数据库自动生成） */
    private String createBy;

    /** 创建时间（@JsonFormat 指定返回前端的JSON时间格式，否则会是时间戳） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 备注 */
    private String remark;

    /** 请求参数（@JsonInclude NON_EMPTY：为空时不序列化到前端，减少JSON体积）
     *  典型内容：beginTime/endTime 时间范围查询参数、dataScope 数据权限SQL片段 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> params;

    public String getSearchValue()
    {
        return searchValue;
    }

    public void setSearchValue(String searchValue)
    {
        this.searchValue = searchValue;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public String getUpdateBy()
    {
        return updateBy;
    }

    public void setUpdateBy(String updateBy)
    {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Map<String, Object> getParams()
    {
        if (params == null)
        {
            params = new HashMap<>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }
}
