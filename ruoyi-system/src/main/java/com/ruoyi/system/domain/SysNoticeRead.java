package com.ruoyi.system.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 公告已读记录表 sys_notice_read
 * <p>
 * 【架构位置】ruoyi-system → domain，对应数据库表 sys_notice_read。
 * 【业务作用】记录「哪个用户在什么时间读了哪条公告」，支撑公告的未读提醒/已读置灰功能。
 * 与 sys_notice（公告主表）是一对多关系：一条公告对应多条已读记录。
 * 【注意】项目定制表（非若依官方标配），未继承 BaseEntity，自带 readTime 记录阅读时间。
 *
 * @author ruoyi
 */
public class SysNoticeRead
{
    /** 主键 */
    private Long readId;

    /** 公告ID：外键关联 sys_notice.notice_id */
    private Long noticeId;

    /** 用户ID：外键关联 sys_user.user_id，谁读的 */
    private Long userId;

    /** 阅读时间：首次打开公告详情的时刻 */
    private Date readTime;

    public Long getReadId()
    {
        return readId;
    }

    public void setReadId(Long readId)
    {
        this.readId = readId;
    }

    public Long getNoticeId()
    {
        return noticeId;
    }

    public void setNoticeId(Long noticeId)
    {
        this.noticeId = noticeId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Date getReadTime()
    {
        return readTime;
    }

    public void setReadTime(Date readTime)
    {
        this.readTime = readTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("readId", getReadId())
            .append("noticeId", getNoticeId())
            .append("userId", getUserId())
            .append("readTime", getReadTime())
            .toString();
    }
}
