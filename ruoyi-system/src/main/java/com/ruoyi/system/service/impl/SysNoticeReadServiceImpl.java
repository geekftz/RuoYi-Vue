package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.domain.SysNoticeRead;
import com.ruoyi.system.mapper.SysNoticeReadMapper;
import com.ruoyi.system.service.ISysNoticeReadService;

/**
 * 公告已读记录 服务层实现
 * <p>
 * 【架构位置】ruoyi-system → service → impl，公告已读业务实现（项目定制模块）。
 * 【核心认知】透传式实现为主；已读/未读的判断不在 Java 内存做，全部下推给 SQL（selectNoticeListWithReadStatus 用 LEFT JOIN 一次查出），
 * 避免「查公告列表再逐条查已读」的 N+1 问题。
 *
 * @author ruoyi
 */
@Service
public class SysNoticeReadServiceImpl implements ISysNoticeReadService
{
    @Autowired
    private SysNoticeReadMapper noticeReadMapper;

    /**
     * 标记已读
     * 【幂等设计】XML 中用 insert ignore / 唯一索引兜底，重复打开同一公告不会产生重复记录
     */
    @Override
    public void markRead(Long noticeId, Long userId)
    {
        SysNoticeRead record = new SysNoticeRead();
        record.setNoticeId(noticeId);
        record.setUserId(userId);
        noticeReadMapper.insertNoticeRead(record);
    }

    /**
     * 查询某用户未读公告数量
     */
    @Override
    public int selectUnreadCount(Long userId)
    {
        return noticeReadMapper.selectUnreadCount(userId);
    }

    /**
     * 查询公告列表并标记当前用户已读状态
     */
    @Override
    public List<SysNotice> selectNoticeListWithReadStatus(Long userId, int limit)
    {
        return noticeReadMapper.selectNoticeListWithReadStatus(userId, limit);
    }

    /**
     * 批量标记已读
     */
    @Override
    public void markReadBatch(Long userId, Long[] noticeIds)
    {
        if (noticeIds == null || noticeIds.length == 0)
        {
            return;
        }
        noticeReadMapper.insertNoticeReadBatch(userId, noticeIds);
    }

    /**
     * 查询已阅读某公告的用户列表
     */
    @Override
    public List<Map<String, Object>> selectReadUsersByNoticeId(Long noticeId, String searchValue)
    {
        return noticeReadMapper.selectReadUsersByNoticeId(noticeId, searchValue);
    }

    /**
     * 删除公告时清理对应已读记录
     */
    @Override
    public void deleteByNoticeIds(Long[] noticeIds)
    {
        noticeReadMapper.deleteByNoticeIds(noticeIds);
    }
}
