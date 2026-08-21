package com.ruoyi.common.utils.sql;

import com.ruoyi.common.exception.UtilException;
import com.ruoyi.common.utils.StringUtils;

/**
 * sql操作工具类
 * <p>
 * 【架构位置】common/utils/sql，SQL注入防护的核心工具。
 * 【能力】校验前端传来的排序字段（orderByColumn）是否包含SQL注入关键字，防止SQL注入攻击。
 * 【为什么需要】MyBatis中ORDER BY子句不能使用#{}预编译参数（只能${}拼接），
 * 前端可控制排序字段名，恶意用户可构造 "id; drop table user--" 之类的注入语句。
 * 【使用方式】ServiceImpl中排序查询前调用 SqlUtil.escapeOrderBySql(orderByColumn) 校验。
 * 【调用链路】前端表格排序 → 传orderByColumn和isAsc → BaseController.startOrderBy() → SqlUtil校验 → 拼入SQL。
 * 
 * @author ruoyi
 */
public class SqlUtil
{
    /**
     * 定义常用的 sql关键字（正则表达式，用|分隔）
     * 覆盖：注释符、逻辑运算符、SQL注入常用函数、危险关键字
     */
    public static String SQL_REGEX = "\u000B|%0A|and |extractvalue|updatexml|sleep|information_schema|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |union |like |+|/*|user()";

    /**
     * 仅支持字母、数字、下划线、空格、逗号、小数点（支持多个字段排序）
     * 合法示例："user_name asc, create_time desc"；不合法示例："id; drop table"
     */
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    /**
     * 限制orderBy最大长度（防止超长字符串攻击）
     */
    private static final int ORDER_BY_MAX_LENGTH = 500;

    /**
     * 检查字符，防止注入绕过
     * 【使用方式】ServiceImpl中：SqlUtil.escapeOrderBySql(orderByColumn)校验排序字段合法性
     * 
     * @param value 前端传来的排序字段值（如 "user_name asc"）
     * @return 校验通过则原样返回，不通过抛UtilException
     * @throws UtilException 参数包含非法字符或超长时抛出
     */
    public static String escapeOrderBySql(String value)
    {
        // 非空且不匹配合法字符集 → 拒绝
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value))
        {
            throw new UtilException("参数不符合规范，不能进行查询");
        }
        // 超长限制 → 拒绝
        if (StringUtils.length(value) > ORDER_BY_MAX_LENGTH)
        {
            throw new UtilException("参数已超过最大限制，不能进行查询");
        }
        return value;
    }

    /**
     * 验证 order by 语法是否符合规范
     * 
     * @param value 排序字段值
     * @return true=合法（只含字母数字下划线空格逗号小数点），false=包含非法字符
     */
    public static boolean isValidOrderBySql(String value)
    {
        return value.matches(SQL_PATTERN);
    }

    /**
     * SQL关键字检查（比escapeOrderBySql更严格的检查，用于普通参数值的注入防护）
     * 【使用场景】对前端传入的查询条件值做关键字过滤
     * 
     * @param value 待检查的参数值
     * @throws UtilException 包含SQL关键字时抛出
     */
    public static void filterKeyword(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return;
        }
        // 去除所有空白字符（防止用空格/制表符绕过关键字匹配）
        String normalizedValue = value.replaceAll("\\p{Z}|\\s", "");
        // 逐个关键字检查是否包含
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords)
        {
            if (StringUtils.indexOfIgnoreCase(normalizedValue, sqlKeyword) > -1)
            {
                throw new UtilException("请求参数包含敏感关键词'" + sqlKeyword + "'，可能存在安全风险");
            }
        }
    }
}
