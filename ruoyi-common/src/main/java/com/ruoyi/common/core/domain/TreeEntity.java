package com.ruoyi.common.core.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree基类
 * <p>
 * 【架构位置】common模块 → core/domain，树形结构实体的公共基类（继承链：TreeEntity → BaseEntity）。
 * 【谁继承它】SysDept（部门）、SysMenu（菜单）等树形表实体。
 * 【树形表设计套路】parentId指父节点 + ancestors存祖先链 + children装子节点，
 *             配合Service层的buildXxxTree()方法即可把数据库平铺List组装成树返回给前端。
 * 
 * @author ruoyi
 */
public class TreeEntity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 父节点名称（非表字段，编辑回显时由Service层单独查出填充，供前端显示用） */
    private String parentName;

    /** 父节点ID（数据库真实字段，树形关联的核心：顶级节点parentId=0） */
    private Long parentId;

    /** 显示顺序 */
    private Integer orderNum;

    /** 祖级列表（如"0,1,3"：从根到本节点所有祖先ID逗号拼接）——核心作用：数据权限过滤和子树查询时用
     *  FIND_IN_SET(dept_id, ancestors)一条SQL即可查出某节点全部子孙，避免递归查库 */
    private String ancestors;

    /** 子节点列表（非表字段，Service层buildXxxTree()组装树时填充；用泛型通配符?兼容SysDept/SysMenu等子类） */
    private List<?> children = new ArrayList<>();

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public String getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    public List<?> getChildren()
    {
        return children;
    }

    public void setChildren(List<?> children)
    {
        this.children = children;
    }
}
