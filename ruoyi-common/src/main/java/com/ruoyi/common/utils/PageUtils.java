package com.ruoyi.common.utils;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.sql.SqlUtil;

/**
 * 分页工具类（若依核心工具 · 重点掌握）
 * <p>
 * 【架构位置】ruoyi-common / utils层 —— 分页能力的统一入口，底层基于 PageHelper 插件。
 * <p>
 * 【日常开发怎么用】Controller返回表格数据的标准三段式：
 *   startPage();                                        // ① 开启分页（本类方法）
 *   List&lt;SysUser&gt; list = userService.selectUserList(user);  // ② 紧跟的第一个查询SQL会被自动拼接 LIMIT
 *   return getDataTable(list);                          // ③ BaseController封装为 {total, rows, code, msg}
 * 【原理】PageHelper 把分页参数（页码/每页条数）存入ThreadLocal，随后第一个MyBatis查询被拦截自动改写为分页SQL；
 * 前端传参：pageNum=1&pageSize=10&orderByColumn=createTime&isAsc=desc。
 * <p>
 * 【注意】startPage() 只对紧跟其后的第一条查询语句生效。
 *
 * @author ruoyi
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     * <p>
     * 流程：TableSupport.buildPageRequest() 从当前请求参数中取出 pageNum/pageSize/orderByColumn/isAsc
     * → SqlUtil.escapeOrderBySql 过滤排序字段（防SQL注入）→ PageHelper开启分页
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        // 【安全】排序字段经过SQL注入过滤，只允许字母数字下划线等安全字符
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        // reasonable：合理化分页，pageNum<1时自动查第一页，超过总页数时查最后一页
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分页的线程变量
     * <p>
     * 分页参数存在ThreadLocal中，异常场景下手动调用防止污染同一线程的下一次查询
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}
