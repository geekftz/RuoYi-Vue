package com.ruoyi.common.core.page;

import com.ruoyi.common.utils.StringUtils;

/**
 * 分页数据
 * <p>
 * 【架构位置】common模块 → core/page，分页参数载体（配合TableSupport、PageUtils使用）。
 * 【调用链路】前端表格组件分页参数(pageNum/pageSize/orderByColumn/isAsc)随请求传来 →
 *             TableSupport.getPageDomain()从当前请求中取出封装为本对象 →
 *             PageUtils.startPage()读取本对象设置PageHelper分页。
 * 【前端联动】前端list接口查询参数：?pageNum=1&pageSize=10&orderByColumn=createTime&isAsc=descending
 * 
 * @author ruoyi
 */
public class PageDomain
{
    /** 当前记录起始索引 */
    private Integer pageNum;

    /** 每页显示记录数 */
    private Integer pageSize;

    /** 排序列 */
    private String orderByColumn;

    /** 排序的方向desc或者asc */
    private String isAsc = "asc";

    /** 分页参数合理化（PageHelper特性：pageNum<1时自动查第一页、pageNum>总页数时自动查最后一页，默认开启） */
    private Boolean reasonable = true;

    /**
     * 组装ORDER BY子句内容（不含"order by"关键字）
     * <p>
     * 【关键处理】StringUtils.toUnderScoreCase()把前端传的驼峰字段名createTime转成数据库列名create_time。
     * 【SQL注入防护】此方法返回值会在PageHelper中拼入SQL，因此TableSupport.buildOrderBy()外层
     *             还有SqlUtil.filterKeyword()过滤非法字符，双重防护排序字段注入。
     * 
     * @return 如 "create_time desc"；无排序字段时返回空串
     */
    public String getOrderBy()
    {
        if (StringUtils.isEmpty(orderByColumn))
        {
            return "";
        }
        return StringUtils.toUnderScoreCase(orderByColumn) + " " + isAsc;
    }

    public Integer getPageNum()
    {
        return pageNum;
    }

    public void setPageNum(Integer pageNum)
    {
        this.pageNum = pageNum;
    }

    public Integer getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
    }

    public String getOrderByColumn()
    {
        return orderByColumn;
    }

    public void setOrderByColumn(String orderByColumn)
    {
        this.orderByColumn = orderByColumn;
    }

    public String getIsAsc()
    {
        return isAsc;
    }

    /**
     * 设置排序方向
     * <p>
     * 【前端联动】Element-UI表格组件的排序事件传来的排序值是"ascending"/"descending"，
     * 这里统一转成SQL关键字"asc"/"desc"，前端开发无需关心转换。
     */
    public void setIsAsc(String isAsc)
    {
        if (StringUtils.isNotEmpty(isAsc))
        {
            // 兼容前端排序类型
            if ("ascending".equals(isAsc))
            {
                isAsc = "asc";
            }
            else if ("descending".equals(isAsc))
            {
                isAsc = "desc";
            }
            this.isAsc = isAsc;
        }
    }

    public Boolean getReasonable()
    {
        if (StringUtils.isNull(reasonable))
        {
            return Boolean.TRUE;
        }
        return reasonable;
    }

    public void setReasonable(Boolean reasonable)
    {
        this.reasonable = reasonable;
    }
}
