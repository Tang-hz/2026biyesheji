package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Notice;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.NoticeService;
import com.gk.study.service.NoticeUserStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 系统公告管理控制器
 * 负责处理系统公告相关的 HTTP 请求
 * 提供公告的查询、创建、删除、更新等功能
 * 所有修改操作需要管理员权限
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    NoticeService service;

    @Autowired
    NoticeUserStateService noticeUserStateService;

    /**
     * 获取公告列表
     * 查询系统中所有的公告信息
     * 
     * @return APIResponse 包含公告列表的响应对象
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<Notice> list =  service.getNoticeList();
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 获取“我的消息”（用户端）：过滤掉该用户已删除的公告
     */
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    public APIResponse userList(Long userId) {
        List<Notice> list = noticeUserStateService.getUserNoticeList(userId);
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 删除一条消息（仅对该用户生效）
     */
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/userDelete", method = RequestMethod.POST)
    public APIResponse userDelete(Long userId, Long noticeId) {
        noticeUserStateService.deleteForUser(userId, noticeId);
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 创建新公告
     * 需要管理员权限
     * 
     * @param notice 公告实体对象，包含公告信息
     * @return APIResponse 创建结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Notice notice) throws IOException {
        service.createNotice(notice);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除公告
     * 需要管理员权限，支持批量删除
     * 
     * @param ids 要删除的公告 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteNotice(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新公告信息
     * 需要管理员权限
     * 
     * @param notice 公告实体对象，包含更新后的公告信息
     * @return APIResponse 更新结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Notice notice) throws IOException {
        service.updateNotice(notice);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

}
