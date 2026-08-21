package com.ruoyi.common.core.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.sql.SqlUtil;

/**
 * Web 层通用数据处理基类
 * <p>
 * 所有 Controller 的父类，提供以下通用能力：
 * - 日期类型自动转换：通过 @InitBinder 将前端传递的日期字符串自动转为 Date 对象
 * - 分页处理：基于 PageHelper 实现物理分页，封装分页参数和结果
 * - 统一响应：提供 success/error/warn 等统一响应方法
 * - 用户信息获取：封装从 SecurityContext 获取当前登录用户信息的方法
 * </p>
 *
 * @author ruoyi
 */
public class BaseController
{
    /** 日志记录器 */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     * <p>
     * 【注解专项】@InitBinder：Spring MVC的数据绑定钩子，在本Controller处理请求前先执行。
     * 这里注册了一个Date类型的自定义编辑器，前端传"2026-08-21"或"2026-08-21 12:00:00"等字符串参数时，
     * 自动调用DateUtils.parseDate()（兼容多种格式）转成Date对象绑定到实体字段上，
     * 避免每个接口手动处理日期字符串。
     */
    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport()
        {
            @Override
            public void setAsText(String text)
            {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 开启分页查询
     * <p>
     * 从请求参数中获取 pageNum、pageSize 等分页参数，
     * 调用 PageHelper.startPage() 设置 ThreadLocal 分页参数。
     * MyBatis 执行下一条查询时会自动拼接 LIMIT 语句实现物理分页。
     * </p>
     * 【若依分页三段式】startPage() → 查询List → getDataTable(list)，缺一不可。
     * 【前端联动】前端列表页传参约定：?pageNum=1&pageSize=10&orderByColumn=createTime&isAsc=descending
     * 【原理】PageHelper将分页参数存在ThreadLocal，仅对紧随其后的第一条MyBatis查询生效，用完即清。
     */
    protected void startPage()
    {
        PageUtils.startPage();
    }

    /**
     * 设置排序条件
     * <p>
     * 从请求参数中获取 orderByColumn 和 isAsc 排序参数，
     * 经 SQL 注入防护处理后，调用 PageHelper.orderBy() 设置排序。
     * </p>
     */
    protected void startOrderBy()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy()))
        {
            // 防止SQL注入，只允许字母、数字、下划线
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * 清理分页的线程变量
     */
    protected void clearPage()
    {
        PageUtils.clearPage();
    }

    /**
     * 封装分页查询结果为 TableDataInfo
     * <p>
     * 将 PageHelper 分页查询的 List 结果封装为统一格式的 TableDataInfo，
     * 包含：状态码、消息、数据列表、总记录数。
     * 总记录数通过 PageInfo(list).getTotal() 获取（PageHelper 会自动拦截 COUNT 查询）。
     * </p>
     *
     * @param list 分页查询结果列表
     * @return 封装后的分页数据对象
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableDataInfo getDataTable(List<?> list)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        // 通过PageInfo获取总记录数（PageHelper拦截COUNT查询获得）
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 返回成功
     */
    public AjaxResult success()
    {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error()
    {
        return AjaxResult.error();
    }

    /**
     * 返回成功消息
     */
    public AjaxResult success(String message)
    {
        return AjaxResult.success(message);
    }
    
    /**
     * 返回成功消息
     */
    public AjaxResult success(Object data)
    {
        return AjaxResult.success(data);
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error(String message)
    {
        return AjaxResult.error(message);
    }

    /**
     * 返回警告消息
     */
    public AjaxResult warn(String message)
    {
        return AjaxResult.warn(message);
    }

    /**
     * 根据影响行数返回操作结果
     * <p>
     * 增删改操作的统一返回方法，影响行数 > 0 返回成功，否则返回失败。
     * </p>
     * 【若依高频用法】新增/修改/删除接口最后一行标配：return toAjax(userService.insertUser(user));
     * MyBatis的insert/update/delete返回受影响行数，>0即成功，前端收到code=200弹出"操作成功"。
     *
     * @param rows 影响的数据库行数
     * @return 操作结果
     */
    protected AjaxResult toAjax(int rows)
    {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 根据布尔结果返回操作结果
     *
     * @param result 操作结果
     * @return AjaxResult
     */
    protected AjaxResult toAjax(boolean result)
    {
        return result ? success() : error();
    }

    /**
     * 页面跳转
     */
    public String redirect(String url)
    {
        return StringUtils.format("redirect:{}", url);
    }

    /**
     * 获取用户缓存信息
     * <p>
     * 【调用链路】SecurityUtils.getLoginUser() → SecurityContextHolder.getContext().getAuthentication()
     * → 强转principal为LoginUser。数据源头是JWT过滤器从Redis取出的登录信息。
     * 【使用场景】Controller里需要当前登录人时直接用，如：record.setCreateBy(getUsername())。
     */
    public LoginUser getLoginUser()
    {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 获取登录用户id
     */
    public Long getUserId()
    {
        return getLoginUser().getUserId();
    }

    /**
     * 获取登录部门id
     */
    public Long getDeptId()
    {
        return getLoginUser().getDeptId();
    }

    /**
     * 获取登录用户名
     */
    public String getUsername()
    {
        return getLoginUser().getUsername();
    }
}
