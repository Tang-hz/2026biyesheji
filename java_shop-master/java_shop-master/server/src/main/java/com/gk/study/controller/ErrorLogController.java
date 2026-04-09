package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.ErrorLog;
import com.gk.study.service.ErrorLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 错误日志管理控制器
 * 负责处理系统错误日志相关的 HTTP 请求
 * 提供错误日志的查询、创建、删除、更新等功能
 * 用于记录和分析系统运行时的错误信息
 */
@Tag(name = "错误日志控制层")
@RestController
@RequestMapping("/errorLog")
public class ErrorLogController {

    private final static Logger logger = LoggerFactory.getLogger(ErrorLogController.class);

    @Autowired
    ErrorLogService service;

    /**
     * 获取错误日志列表
     * 查询系统中所有的错误日志记录
     * 
     * @return APIResponse 包含错误日志列表的响应对象
     */
    @Operation(summary = "错误日志列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<ErrorLog> list =  service.getErrorLogList();
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 创建错误日志记录
     * 用于记录系统运行时发生的错误信息
     * 
     * @param errorLog 错误日志实体对象，包含错误详细信息
     * @return APIResponse 创建结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Operation(summary = "新增错误日志")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(ErrorLog errorLog) throws IOException {
        service.createErrorLog(errorLog);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除错误日志
     * 支持批量删除错误日志记录
     * 
     * @param ids 要删除的错误日志 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @Operation(summary = "批量删除错误日志")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteErrorLog(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新错误日志信息
     * 
     * @param errorLog 错误日志实体对象，包含更新后的错误信息
     * @return APIResponse 更新结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Operation(summary = "更新错误日志")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(ErrorLog errorLog) throws IOException {
        service.updateErrorLog(errorLog);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

}
