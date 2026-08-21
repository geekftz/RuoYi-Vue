package com.ruoyi.system.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;

/**
 * 通知公告表 sys_notice
 * <p>
 * 【架构位置】ruoyi-system → domain，对应数据库表 sys_notice（通知公告）。
 * 【业务作用】「系统管理-通知公告」菜单的数据载体，管理员发布后用户在首页/消息中心查看。
 * 【注意】noticeContent 是富文本 HTML，前端用 v-html 渲染，因此入库前要靠 @Xss 过滤脚本（见 getter 注解）。
 * 
 * @author ruoyi
 */
public class SysNotice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 公告ID：主键 */
    private Long noticeId;

    /** 公告标题 */
    private String noticeTitle;

    /** 公告类型（1通知 2公告）：对应字典 sys_notice_type，前端用字典标签展示 */
    private String noticeType;

    /** 公告内容：富文本 HTML 字符串 */
    private String noticeContent;

    /** 公告状态（0正常 1关闭）：对应字典 sys_notice_status，关闭后前端不再展示 */
    private String status;

    /** 是否已读：非表字段！由 SysNoticeService 联查 sys_notice_read 动态填充，@JsonProperty 保证序列化后前端拿到 isRead 属性名 */
    @JsonProperty("isRead")
    private boolean isRead;

    public Long getNoticeId()
    {
        return noticeId;
    }

    public void setNoticeId(Long noticeId)
    {
        this.noticeId = noticeId;
    }

    public void setNoticeTitle(String noticeTitle)
    {
        this.noticeTitle = noticeTitle;
    }

    @Xss(message = "公告标题不能包含脚本字符")
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 0, max = 50, message = "公告标题不能超过50个字符")
    // 校验注解标在 getter 上是若依惯例：配合 Controller 入参 @Validated，由 Spring 在绑定后逐 getter 触发校验
    public String getNoticeTitle()
    {
        return noticeTitle;
    }

    public void setNoticeType(String noticeType)
    {
        this.noticeType = noticeType;
    }

    public String getNoticeType()
    {
        return noticeType;
    }

    public void setNoticeContent(String noticeContent)
    {
        this.noticeContent = noticeContent;
    }

    public String getNoticeContent()
    {
        return noticeContent;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public boolean getIsRead()
    {
        return isRead;
    }

    public void setIsRead(boolean isRead)
    {
        this.isRead = isRead;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("noticeId", getNoticeId())
            .append("noticeTitle", getNoticeTitle())
            .append("noticeType", getNoticeType())
            .append("noticeContent", getNoticeContent())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
