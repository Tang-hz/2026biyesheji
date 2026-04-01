package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Comment;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 评论管理控制器
 * 负责处理商品评论相关的 HTTP 请求
 * 提供评论的查询、创建、删除、更新、点赞等功能
 * 支持按商品和用户查询评论
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final static Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    CommentService service;

    /**
     * 获取评论列表
     * 查询系统中所有的评论信息
     * 
     * @return APIResponse 包含评论列表的响应对象
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<Comment> list =  service.getCommentList();
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }


    /**
     * 获取商品的所有评论
     * 根据商品 ID 查询该商品的所有评论，支持排序
     * 
     * @param thingId 商品 ID
     * @param order 排序方式
     * @return APIResponse 包含评论列表的响应对象
     */
    @RequestMapping(value = "/listThingComments", method = RequestMethod.GET)
    public APIResponse listThingComments(String thingId, String order){
        List<Comment> list =  service.getThingCommentList(thingId, order);
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 获取用户的所有评论
     * 根据用户 ID 查询该用户的所有评论
     * 
     * @param userId 用户 ID
     * @return APIResponse 包含评论列表的响应对象
     */
    @RequestMapping(value = "/listUserComments", method = RequestMethod.GET)
    public APIResponse listUserComments(String userId){
        List<Comment> list =  service.getUserCommentList(userId);
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 创建新评论
     * 
     * @param comment 评论实体对象，包含评论信息
     * @return APIResponse 创建结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Comment comment) throws IOException {
        service.createComment(comment);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除评论
     * 需要管理员权限，支持批量删除
     * 
     * @param ids 要删除的评论 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteComment(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新评论信息
     * 需要管理员权限
     * 
     * @param comment 评论实体对象，包含更新后的评论信息
     * @return APIResponse 更新结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Comment comment) throws IOException {
        service.updateComment(comment);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

    /**
     * 点赞评论
     * 增加评论的点赞数
     * 
     * @param id 评论 ID
     * @return APIResponse 点赞结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @RequestMapping(value = "/like", method = RequestMethod.POST)
    @Transactional
    public APIResponse like(String id) throws IOException {
        Comment commentBean = service.getCommentDetail(id);
        int likeCount = Integer.parseInt(commentBean.getLikeCount()) + 1;
        commentBean.setLikeCount(String.valueOf(likeCount));
        service.updateComment(commentBean);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

}
