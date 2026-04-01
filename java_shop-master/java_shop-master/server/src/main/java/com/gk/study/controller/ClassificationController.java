package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Classification;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.ClassificationService;
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
 * 商品分类管理控制器
 * 负责处理商品分类相关的 HTTP 请求
 * 提供分类的查询、创建、删除、更新等功能
 * 所有修改操作需要管理员权限
 */
@RestController
@RequestMapping("/classification")
public class ClassificationController {

    private final static Logger logger = LoggerFactory.getLogger(ClassificationController.class);

    @Autowired
    ClassificationService service;

    /**
     * 获取分类列表
     * 查询系统中所有的商品分类信息
     * 
     * @return APIResponse 包含分类列表的响应对象
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<Classification> list =  service.getClassificationList();
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 创建新分类
     * 需要管理员权限
     * 
     * @param classification 分类实体对象，包含分类信息
     * @return APIResponse 创建结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Classification classification) throws IOException {
        service.createClassification(classification);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除分类
     * 需要管理员权限，支持批量删除
     * 
     * @param ids 要删除的分类 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteClassification(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新分类信息
     * 需要管理员权限
     * 
     * @param classification 分类实体对象，包含更新后的分类信息
     * @return APIResponse 更新结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Classification classification) throws IOException {
        service.updateClassification(classification);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

}
