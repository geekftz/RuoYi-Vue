package com.ruoyi.common.core.page;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.ServletUtils;

/**
 * 表格数据处理
 * <p>
 * 【架构位置】ruoyi-common / core / page层 —— 分页参数的"搬运工"。
 * 【职责】从当前HTTP请求参数中取出分页相关参数（pageNum/pageSize/orderByColumn/isAsc/reasonable），
 * 组装成 PageDomain 对象，供 PageUtils.startPage() 开启 PageHelper 分页。
 * 【前端约定】列表页请求自带 ?pageNum=1&pageSize=10&orderByColumn=create_time&isAsc=desc。
 *
 * @author ruoyi
 */
public class TableSupport
{
    /**
     * 当前记录起始索引（前端分页器当前页码，默认1）
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数（默认10）
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列（对应数据库列名，如 create_time）
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或者 "asc".
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 分页参数合理化（pageNum越界时自动修正为首页/末页）
     */
    public static final String REASONABLE = "reasonable";

    /**
     * 封装分页对象：从当前请求线程取参数 → PageDomain（取不到时给默认值 1/10）
     */
    public static PageDomain getPageDomain()
    {
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(Convert.toInt(ServletUtils.getParameter(PAGE_NUM), 1));
        pageDomain.setPageSize(Convert.toInt(ServletUtils.getParameter(PAGE_SIZE), 10));
        pageDomain.setOrderByColumn(ServletUtils.getParameter(ORDER_BY_COLUMN));
        pageDomain.setIsAsc(ServletUtils.getParameter(IS_ASC));
        pageDomain.setReasonable(ServletUtils.getParameterToBool(REASONABLE));
        return pageDomain;
    }

    public static PageDomain buildPageRequest()
    {
        return getPageDomain();
    }
}
