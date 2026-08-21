package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.ISysDeptService;

/**
 * 部门管理服务实现
 * <p>
 * 管理部门树形结构（sys_dept表），包括：
 * - 构建部门树（递归查询子部门）
 * - 新增/修改/删除部门（删除时检查子部门是否存在）
 * - 校验部门名称唯一性
 * - 查询部门下的子部门数量
 * </p>
 * 【架构位置】业务层 ServiceImpl，对应 SysDeptController。
 * 【重点掌握】本类集中体现了若依两大核心机制：
 * 1. @DataScope 数据权限：不同角色看到不同范围的部门数据；
 * 2. ancestors 祖级列表字段：冗余存储"0,100,101"式路径，树查询/子级批量更新都靠它，
 *    是用空间换时间的经典设计，避免递归 SQL。
 *
 * @author ruoyi
 */
@Service
public class SysDeptServiceImpl implements ISysDeptService
{
    /** 部门 Mapper */
    @Autowired
    private SysDeptMapper deptMapper;

    /** 角色 Mapper */
    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 查询部门管理数据
     * 
     * @param dept 部门信息
     * @return 部门信息集合
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysDept> selectDeptList(SysDept dept)
    {
        // 【注解专项】@DataScope：若依数据权限注解。DataScopeAspect 切面会在方法执行前
        // 根据当前登录用户的角色数据范围（全部/本部门/本部门及以下/自定义），
        // 自动往 SQL 末尾拼接 AND d.dept_id IN (...) 过滤条件，实现"不同人看到不同部门"。
        // deptAlias = "d" 对应 SysDeptMapper.xml 中部门表的别名。
        return deptMapper.selectDeptList(dept);
    }

    /**
     * 查询部门树结构信息
     * 
     * @param dept 部门信息
     * @return 部门树信息集合
     */
    @Override
    public List<TreeSelect> selectDeptTreeList(SysDept dept)
    {
        // 【难点】SpringUtils.getAopProxy(this)：获取当前 Bean 的 AOP 代理对象再调用 selectDeptList。
        // 为什么不用 this.selectDeptList()？因为 @DataScope 是 AOP 切面实现的，
        // 同类内部直接 this 调用会绕过代理、切面失效，数据权限过滤不生效。
        // 通过代理对象调用才能触发 DataScopeAspect。这是 Spring AOP 的经典坑。
        List<SysDept> depts = SpringUtils.getAopProxy(this).selectDeptList(dept);
        // 把扁平列表组装成前端 el-tree 需要的 {id, label, children} 树结构
        return buildDeptTreeSelect(depts);
    }

    /**
     * 构建前端所需要树结构
     * 
     * @param depts 部门列表
     * @return 树结构列表
     */
    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts)
    {
        // 内存建树（buildTree 类工具的典型实现）：查出的是扁平列表，这里组装成父子嵌套结构
        List<SysDept> returnList = new ArrayList<SysDept>();
        // 所有部门ID集合，用于判断某部门的父ID是否在列表中
        List<Long> tempList = depts.stream().map(SysDept::getDeptId).collect(Collectors.toList());
        for (SysDept dept : depts)
        {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            // 判断依据：父ID不在当前列表中 → 说明它是本批数据的根（数据权限过滤后可能拿不到真根0）
            if (!tempList.contains(dept.getParentId()))
            {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty())
        {
            // 极端情况（如循环数据）下兜底返回原列表，避免前端拿到空树
            returnList = depts;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts)
    {
        List<SysDept> deptTrees = buildDeptTree(depts);
        // TreeSelect 是若依通用的下拉树 VO：{id, label, children}，正好匹配前端 el-tree-select 组件
        // 构造方法 TreeSelect(SysDept) 内部取 deptId→id、deptName→label
        return deptTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询部门树信息
     * 
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId)
    {
        SysRole role = roleMapper.selectRoleById(roleId);
        // deptCheckStrictly：角色编辑页"部门权限"树的父子联动开关
        // true=勾选父节点自动选子节点（回显需过滤掉有父级的部门，否则前端树会全勾上）
        return deptMapper.selectDeptListByRoleId(roleId, role.isDeptCheckStrictly());
    }

    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    @Override
    public SysDept selectDeptById(Long deptId)
    {
        return deptMapper.selectDeptById(deptId);
    }

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Override
    public int selectNormalChildrenDeptById(Long deptId)
    {
        return deptMapper.selectNormalChildrenDeptById(deptId);
    }

    /**
     * 是否存在子节点
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public boolean hasChildByDeptId(Long deptId)
    {
        int result = deptMapper.hasChildByDeptId(deptId);
        return result > 0;
    }

    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkDeptExistUser(Long deptId)
    {
        int result = deptMapper.checkDeptExistUser(deptId);
        return result > 0;
    }

    /**
     * 校验部门名称是否唯一
     * 
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public boolean checkDeptNameUnique(SysDept dept)
    {
        Long deptId = StringUtils.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        SysDept info = deptMapper.checkDeptNameUnique(dept.getDeptName(), dept.getParentId());
        if (StringUtils.isNotNull(info) && info.getDeptId().longValue() != deptId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验部门是否有数据权限
     * 
     * @param deptId 部门id
     */
    @Override
    public void checkDeptDataScope(Long deptId)
    {
        // 非管理员才需要校验：用带 @DataScope 的查询验证目标部门是否在当前用户数据权限范围内
        // 查不到 → 说明越权访问，抛异常拦截
        if (!SecurityUtils.isAdmin() && StringUtils.isNotNull(deptId))
        {
            SysDept dept = new SysDept();
            dept.setDeptId(deptId);
            // 同样必须用代理对象调用才能触发 @DataScope 切面
            List<SysDept> depts = SpringUtils.getAopProxy(this).selectDeptList(dept);
            if (StringUtils.isEmpty(depts))
            {
                throw new ServiceException("没有权限访问部门数据！");
            }
        }
    }

    /**
     * 新增保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int insertDept(SysDept dept)
    {
        SysDept info = deptMapper.selectDeptById(dept.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!UserConstants.DEPT_NORMAL.equals(info.getStatus()))
        {
            throw new ServiceException("部门停用，不允许新增");
        }
        // 维护 ancestors 祖级路径：父部门的 ancestors + 父部门ID，如 "0,100,101"
        // 有了这个冗余字段，查"某部门下所有子部门"只需 LIKE 'ancestors%'，无需递归
        dept.setAncestors(info.getAncestors() + "," + dept.getParentId());
        return deptMapper.insertDept(dept);
    }

    /**
     * 修改保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int updateDept(SysDept dept)
    {
        SysDept newParentDept = deptMapper.selectDeptById(dept.getParentId());
        SysDept oldDept = deptMapper.selectDeptById(dept.getDeptId());
        if (StringUtils.isNotNull(newParentDept) && StringUtils.isNotNull(oldDept))
        {
            // 部门换父级时，要重算 ancestors 并同步更新所有子孙部门的路径
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
            String oldAncestors = oldDept.getAncestors();
            dept.setAncestors(newAncestors);
            updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
        }
        int result = deptMapper.updateDept(dept);
        if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StringUtils.isNotEmpty(dept.getAncestors())
                && !StringUtils.equals("0", dept.getAncestors()))
        {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            // （避免出现"父停用子启用"导致树结构展示异常）
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     * 
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDept dept)
    {
        String ancestors = dept.getAncestors();
        Long[] deptIds = Convert.toLongArray(ancestors);
        deptMapper.updateDeptStatusNormal(deptIds);
    }

    /**
     * 修改子元素关系
     * 
     * @param deptId 被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors)
    {
        // 查询所有子孙部门（SQL 用 find_in_set(deptId, ancestors) 实现，全靠 ancestors 冗余字段）
        List<SysDept> children = deptMapper.selectChildrenDeptById(deptId);
        for (SysDept child : children)
        {
            // 字符串替换前缀：把旧祖级路径换成新路径，如 0,100 → 0,105,100
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0)
        {
            // 批量更新（XML 中用 foreach 拼 CASE WHEN）
            deptMapper.updateDeptChildren(children);
        }
    }

    /**
     * 保存部门排序
     *
     * @param deptIds 部门ID数组
     * @param orderNums 排序数组
     */
    @Override
    @Transactional
    public void updateDeptSort(String[] deptIds, String[] orderNums)
    {
        // 【事务范围】批量更新多条记录，任一失败整体回滚，避免出现"排一半"的中间状态
        try
        {
            for (int i = 0; i < deptIds.length; i++)
            {
                SysDept dept = new SysDept();
                dept.setDeptId(Convert.toLong(deptIds[i]));
                dept.setOrderNum(Convert.toInt(orderNums[i]));
                deptMapper.updateDeptSort(dept);
            }
        }
        catch (Exception e)
        {
            // 捕获后转成业务异常抛出（注意：抛出 RuntimeException 子类 ServiceException 仍会触发事务回滚）
            throw new ServiceException("保存排序异常，请联系管理员");
        }
    }

    /**
     * 删除部门管理信息
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public int deleteDeptById(Long deptId)
    {
        return deptMapper.deleteDeptById(deptId);
    }

    /**
     * 递归列表
     * 【原理】经典内存递归建树：给当前节点 t 找到直接子节点，挂到 t.children，
     * 再对每个子节点递归。时间复杂度 O(n²) 级别，但部门数量通常几百以内，性能足够。
     */
    private void recursionFn(List<SysDept> list, SysDept t)
    {
        // 得到子节点列表
        List<SysDept> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysDept tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysDept> getChildList(List<SysDept> list, SysDept t)
    {
        List<SysDept> tlist = new ArrayList<SysDept>();
        Iterator<SysDept> it = list.iterator();
        while (it.hasNext())
        {
            SysDept n = (SysDept) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysDept> list, SysDept t)
    {
        return getChildList(list, t).size() > 0;
    }
}
