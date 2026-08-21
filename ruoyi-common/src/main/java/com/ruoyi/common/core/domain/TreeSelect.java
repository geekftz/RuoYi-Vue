package com.ruoyi.common.core.domain;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.utils.StringUtils;

/**
 * Treeselect树结构实体类
 * <p>
 * 【架构位置】common模块 → core/domain，树形下拉框的专用VO（视图对象）。
 * 【前端联动】字段命名id/label/children严格对齐前端Element-UI树组件（el-tree/el-tree-select）的默认props约定，
 *             后端组装好直接返回，前端零转换即可渲染。
 * 【使用场景】部门树选择（/system/dept/treeselect）、菜单树选择（/system/menu/treeselect）等接口的返回元素。
 * 【调用链路】Service层查出List<SysDept>/List<SysMenu>平铺列表 → buildDeptTree/buildMenuTree组装成树 →
 *             stream().map(TreeSelect::new)逐个转换为本VO → 返回前端。
 * 
 * @author ruoyi
 */
public class TreeSelect implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 节点ID */
    private Long id;

    /** 节点名称 */
    private String label;

    /** 节点禁用 */
    private boolean disabled = false;

    /** 子节点 */
    // 【注解专项】@JsonInclude(NON_EMPTY)：Jackson序列化时若children为空集合则不输出该字段，
    // 让返回给前端的JSON更干净（叶子节点不带"children":[]），前端树组件也能正确识别叶子。
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    public TreeSelect()
    {

    }

    /**
     * 由部门实体递归转换为树节点
     * <p>
     * 【递归原理】children字段通过stream().map(TreeSelect::new)对每个子部门再次调用本构造方法，
     * 一层层向下直到叶子节点，一次性把整棵部门树转成VO树。
     * 【disabled】停用状态的部门置灰不可选（如新增用户时不允许挂到已停用部门下）。
     */
    public TreeSelect(SysDept dept)
    {
        this.id = dept.getDeptId();
        this.label = dept.getDeptName();
        this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus());
        this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /** 由菜单实体递归转换为树节点（同上，用于角色分配菜单等场景） */
    public TreeSelect(SysMenu menu)
    {
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();
        this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    public List<TreeSelect> getChildren()
    {
        return children;
    }

    public void setChildren(List<TreeSelect> children)
    {
        this.children = children;
    }
}
