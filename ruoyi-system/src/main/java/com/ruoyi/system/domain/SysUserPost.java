package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户和岗位关联 sys_user_post
 * <p>
 * 【架构位置】ruoyi-system → domain，对应数据库中间表 sys_user_post（用户-岗位多对多关联）。
 * 【业务作用】记录用户拥有哪些岗位（一个人可兼多岗），用户管理页「岗位」多选框的数据落点。
 * 【典型场景】新增/编辑用户时随用户表单一起提交 postIds 数组，ServiceImpl 先删后插本表。
 * 【注意】纯关联表，只有两列外键，无主键无审计字段，所以不继承 BaseEntity。
 * 
 * @author ruoyi
 */
public class SysUserPost
{
    /** 用户ID：外键关联 sys_user.user_id */
    private Long userId;
    
    /** 岗位ID：外键关联 sys_post.post_id */
    private Long postId;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getPostId()
    {
        return postId;
    }

    public void setPostId(Long postId)
    {
        this.postId = postId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("postId", getPostId())
            .toString();
    }
}
