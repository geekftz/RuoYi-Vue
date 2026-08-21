package com.ruoyi.common.core.page;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象（若依统一返回体之二 · 重点掌握）
 * <p>
 * 【架构位置】ruoyi-common / core / page层 —— Controller表格列表接口的统一返回结构。
 * 【返回给前端的JSON格式】{"code":200,"msg":"查询成功","total":100,"rows":[{...},{...}]}
 * 【前端约定】ruoyi-ui 列表页 axios 后直接用 total 渲染分页器、rows 渲染 el-table 数据。
 * <p>
 * 【构造方】业务代码不直接new它：BaseController.getDataTable(list) 配合 PageHelper
 * 从分页插件的 Page 对象中取出总记录数自动组装。
 *
 * @author ruoyi
 */
public class TableDataInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 总记录数（满足条件的全部条数，不是当前页条数；前端分页器据此算总页数） */
    private long total;

    /** 列表数据（当前页的数据行） */
    private List<?> rows;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    /**
     * 表格数据对象
     */
    public TableDataInfo()
    {
    }

    /**
     * 分页
     * 
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total)
    {
        this.rows = list;
        this.total = total;
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public List<?> getRows()
    {
        return rows;
    }

    public void setRows(List<?> rows)
    {
        this.rows = rows;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }
}
