package com.ruoyi.framework.aspectj;

import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.security.context.PermissionContextHolder;

/**
 * 数据权限过滤切面
 * <p>
 * 通过 AOP 拦截带有 @DataScope 注解的 Controller 方法，在查询执行前自动拼接数据权限 SQL 条件，
 * 实现不同角色看到不同范围的数据。
 * </p>
 * <p>
 * 数据权限范围（data_scope 字段值）：
 * - 1：全部数据权限 —— 不加任何限制条件
 * - 2：自定义数据权限 —— 通过 sys_role_dept 关联表查询可见部门
 * - 3：本部门数据权限 —— 只查询用户所属部门的数据
 * - 4：本部门及以下数据权限 —— 查询本部门及所有子部门的数据
 * - 5：仅本人数据权限 —— 只查询自己创建的数据
 * </p>
 * <p>
 * 实现原理：将拼接好的 SQL WHERE 条件存入 BaseEntity.params.dataScope 中，
 * 在 MyBatis 的 XML 映射文件中通过 ${params.dataScope} 引用。
 * </p>
 *
 * @author ruoyi
 */
@Aspect
@Component
public class DataScopeAspect
{
    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";

    /**
     * 前置通知，执行数据权限过滤
     */
    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope) throws Throwable
    {
        clearDataScope(point);
        handleDataScope(point, controllerDataScope);
    }

    /**
     * 数据权限过滤处理逻辑
     * <p>
     * 获取当前登录用户，若为超级管理员（admin）则跳过过滤（拥有全部数据权限），
     * 否则根据用户角色拼接数据权限 SQL 条件。
     * </p>
     *
     * @param joinPoint           切点
     * @param controllerDataScope 数据权限注解（包含部门别名、用户别名等配置）
     */
    protected void handleDataScope(final JoinPoint joinPoint, DataScope controllerDataScope)
    {
        // 获取当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNotNull(loginUser))
        {
            SysUser currentUser = loginUser.getUser();
            // 超级管理员拥有全部数据权限，不做过滤
            if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin())
            {
                // 获取权限标识：优先使用注解上配置的，否则从ThreadLocal上下文获取
                String permission = StringUtils.defaultIfEmpty(controllerDataScope.permission(), PermissionContextHolder.getContext());
                // 执行数据权限过滤，拼接SQL条件
                dataScopeFilter(joinPoint, currentUser, controllerDataScope.userAlias(), controllerDataScope.deptAlias(), controllerDataScope.userField(), controllerDataScope.deptField(), permission);
            }
        }
    }

    /**
     * 数据范围过滤核心方法：根据用户角色拼接 SQL WHERE 条件
     * <p>
     * 遍历用户的所有角色，根据每个角色的 dataScope（数据范围类型）拼接不同的 SQL 条件：
     * - DATA_SCOPE_ALL（1）：清空所有条件，拥有全部数据权限
     * - DATA_SCOPE_CUSTOM（2）：通过 sys_role_dept 关联表查询自定义部门
     * - DATA_SCOPE_DEPT（3）：限定为用户所属部门
     * - DATA_SCOPE_DEPT_AND_CHILD（4）：限定为用户所属部门及所有子部门（使用 find_in_set 查询 ancestors）
     * - DATA_SCOPE_SELF（5）：限定为仅本人的数据
     * </p>
     * <p>
     * 多角色取并集（OR），最终将拼接好的条件存入 BaseEntity.params.dataScope，
     * 在 MyBatis XML 中通过 ${params.dataScope} 引用。
     * </p>
     *
     * @param joinPoint   切点
     * @param user        当前登录用户
     * @param userAlias   用户表别名（如 u）
     * @param deptAlias   部门表别名（如 d）
     * @param userField    用户表字段名（如 user_id）
     * @param deptField    部门表字段名（如 dept_id）
     * @param permission  权限标识（用于筛选匹配该权限的角色）
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, String userAlias, String deptAlias, String userField, String deptField, String permission)
    {
        StringBuilder sqlString = new StringBuilder();
        // 记录已处理的数据权限类型，避免重复拼接
        List<String> conditions = new ArrayList<String>();
        // 收集所有自定义数据权限角色的ID，用于批量IN查询优化
        List<String> scopeCustomIds = new ArrayList<String>();
        // 预先收集所有自定义数据权限角色ID
        user.getRoles().forEach(role -> {
            if (Constants.Dept.DATA_SCOPE_CUSTOM.equals(role.getDataScope()) && StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL) && (StringUtils.isEmpty(permission) || StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission))))
            {
                scopeCustomIds.add(Convert.toStr(role.getRoleId()));
            }
        });

        // 遍历用户的每个角色，根据数据范围类型拼接SQL条件
        for (SysRole role : user.getRoles())
        {
            String dataScope = role.getDataScope();
            // 跳过已处理的数据权限类型和已停用的角色
            if (conditions.contains(dataScope) || StringUtils.equals(role.getStatus(), UserConstants.ROLE_DISABLE))
            {
                continue;
            }
            // 如果指定了权限标识，且角色不包含该权限，则跳过
            if (StringUtils.isNotEmpty(permission) && !StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission)))
            {
                continue;
            }
            if (Constants.Dept.DATA_SCOPE_ALL.equals(dataScope))
            {
                // 全部数据权限：清空所有条件，直接跳出循环
                sqlString = new StringBuilder();
                conditions.add(dataScope);
                break;
            }
            else if (Constants.Dept.DATA_SCOPE_CUSTOM.equals(dataScope))
            {
                // 自定义数据权限：通过 sys_role_dept 关联表查询角色关联的部门
                if (scopeCustomIds.size() > 1)
                {
                    // 多个自定义角色使用IN查询优化性能
                    sqlString.append(StringUtils.format(" OR {}.{} IN ( SELECT dept_id FROM sys_role_dept WHERE role_id in ({}) ) ", deptAlias, deptField, String.join(",", scopeCustomIds)));
                }
                else
                {
                    sqlString.append(StringUtils.format(" OR {}.{} IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias, deptField, role.getRoleId()));
                }
            }
            else if (Constants.Dept.DATA_SCOPE_DEPT.equals(dataScope))
            {
                // 本部门数据权限：限定为用户所属部门
                sqlString.append(StringUtils.format(" OR {}.{} = {} ", deptAlias, deptField, user.getDeptId()));
            }
            else if (Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope))
            {
                // 本部门及子部门：使用 find_in_set 查询 ancestors 字段中包含本部门ID的所有部门
                sqlString.append(StringUtils.format(" OR {}.{} IN ( SELECT dept_id FROM sys_dept WHERE dept_id = {} or find_in_set( {} , ancestors ) )", deptAlias, deptField, user.getDeptId(), user.getDeptId()));
            }
            else if (Constants.Dept.DATA_SCOPE_SELF.equals(dataScope))
            {
                // 仅本人数据权限：限定为用户自己的数据
                if (StringUtils.isNotBlank(userAlias))
                {
                    sqlString.append(StringUtils.format(" OR {}.{} = {} ", userAlias, userField, user.getUserId()));
                }
                else
                {
                    // 没有配置用户别名时，查询不到任何数据
                    sqlString.append(StringUtils.format(" OR {}.{} = 0 ", deptAlias, deptField));
                }
            }
            conditions.add(dataScope);
        }

        // 如果没有任何角色匹配到权限标识，则限制查询不到任何数据
        if (StringUtils.isEmpty(conditions))
        {
            sqlString.append(StringUtils.format(" OR {}.{} = 0 ", deptAlias, deptField));
        }

        // 将拼接好的SQL条件存入方法参数的 BaseEntity.params 中
        if (StringUtils.isNotBlank(sqlString.toString()))
        {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
            {
                BaseEntity baseEntity = (BaseEntity) params;
                // 去掉开头的" OR "，用 AND ( ... ) 包裹
                baseEntity.getParams().put(DATA_SCOPE, " AND (" + sqlString.substring(4) + ")");
            }
        }
    }

    /**
     * 清空数据权限参数（防止SQL注入）
     * <p>
     * 在拼接权限 SQL 之前，先清空 params.dataScope 参数，
     * 防止前端恶意传入 dataScope 参数进行 SQL 注入。
     * </p>
     *
     * @param joinPoint 切点
     */
    private void clearDataScope(final JoinPoint joinPoint)
    {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
        {
            BaseEntity baseEntity = (BaseEntity) params;
            baseEntity.getParams().put(DATA_SCOPE, "");
        }
    }
}
