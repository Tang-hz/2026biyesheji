package com.gk.study.service;

import com.gk.study.entity.Notice;

import java.util.List;

public interface NoticeUserStateService {

    /**
     * 获取“我的消息”：过滤掉该用户已删除的公告
     */
    List<Notice> getUserNoticeList(Long userId);

    /**
     * 删除（仅对该用户生效）
     */
    void deleteForUser(Long userId, Long noticeId);
}

