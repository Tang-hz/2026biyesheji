package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gk.study.entity.Notice;
import com.gk.study.entity.NoticeUserState;
import com.gk.study.mapper.NoticeMapper;
import com.gk.study.mapper.NoticeUserStateMapper;
import com.gk.study.service.NoticeUserStateService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeUserStateServiceImpl implements NoticeUserStateService {

    private final NoticeMapper noticeMapper;
    private final NoticeUserStateMapper noticeUserStateMapper;

    public NoticeUserStateServiceImpl(NoticeMapper noticeMapper, NoticeUserStateMapper noticeUserStateMapper) {
        this.noticeMapper = noticeMapper;
        this.noticeUserStateMapper = noticeUserStateMapper;
    }

    @Override
    public List<Notice> getUserNoticeList(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 取出用户已删除的 noticeId 集合
        QueryWrapper<NoticeUserState> deletedQw = new QueryWrapper<>();
        deletedQw.eq("user_id", userId).eq("deleted", 1);
        List<NoticeUserState> deleted = noticeUserStateMapper.selectList(deletedQw);
        List<Long> deletedIds = deleted.stream().map(NoticeUserState::getNoticeId).collect(Collectors.toList());

        QueryWrapper<Notice> noticeQw = new QueryWrapper<>();
        if (!deletedIds.isEmpty()) {
            noticeQw.notIn("id", deletedIds);
        }
        // 按创建时间倒序（create_time 是 varchar，项目里存的是毫秒时间戳字符串）
        noticeQw.orderByDesc("create_time");
        return noticeMapper.selectList(noticeQw);
    }

    @Override
    public void deleteForUser(Long userId, Long noticeId) {
        if (userId == null || noticeId == null) {
            return;
        }

        QueryWrapper<NoticeUserState> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("notice_id", noticeId);
        NoticeUserState state = noticeUserStateMapper.selectOne(qw);
        if (state == null) {
            state = new NoticeUserState();
            state.setUserId(userId);
            state.setNoticeId(noticeId);
            state.setDeleted(1);
            state.setDeleteTime(String.valueOf(System.currentTimeMillis()));
            noticeUserStateMapper.insert(state);
        } else {
            state.setDeleted(1);
            state.setDeleteTime(String.valueOf(System.currentTimeMillis()));
            noticeUserStateMapper.updateById(state);
        }
    }
}

