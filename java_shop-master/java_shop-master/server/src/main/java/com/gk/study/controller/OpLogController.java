package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.OpLog;
import com.gk.study.service.OpLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

// 负责操作日志和登录日志
/**
 * 操作日志管理控制器
 * 负责处理系统操作日志和登录日志相关的 HTTP 请求
 * 提供操作日志和登录日志的查询、创建、删除、更新等功能
 * 用于追踪用户操作记录和系统安全审计
 */
@RestController
@RequestMapping("/opLog")
public class OpLogController {

    private final static Logger logger = LoggerFactory.getLogger(OpLogController.class);

    @Autowired
    OpLogService service;

    /**
     * 获取操作日志列表
     * 查询系统中所有的操作日志记录
     * 
     * @return APIResponse 包含操作日志列表的响应对象
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<OpLog> list =  service.getOpLogList();
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 获取登录日志列表
     * 查询系统中所有的用户登录日志记录
     * 
     * @return APIResponse 包含登录日志列表的响应对象
     */
    @RequestMapping(value = "/loginLogList", method = RequestMethod.GET)
    public APIResponse loginLogList(){
        List<OpLog> list =  service.getLoginLogList();
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 创建操作日志记录
     * 用于记录用户在系统中的操作行为
     * 
     * @param opLog 操作日志实体对象，包含操作详细信息
     * @return APIResponse 创建结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(OpLog opLog) throws IOException {
        service.createOpLog(opLog);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除操作日志
     * 支持批量删除操作日志记录
     * 
     * @param ids 要删除的操作日志 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteOpLog(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新操作日志信息
     * 
     * @param opLog 操作日志实体对象，包含更新后的操作信息
     * @return APIResponse 更新结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(OpLog opLog) throws IOException {
        service.updateOpLog(opLog);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

}
