package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Tag;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理控制层")
@RestController
@RequestMapping("/tag")
public class TagController {

    private final static Logger logger = LoggerFactory.getLogger(TagController.class);

    @Autowired
    TagService service;

    @Operation(summary = "标签列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<Tag> list =  service.getTagList();

        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    @Operation(summary = "新增标签")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Tag tag) throws IOException {
        service.createTag(tag);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    @Operation(summary = "批量删除标签")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteTag(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    @Operation(summary = "更新标签")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Tag tag) throws IOException {
        service.updateTag(tag);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

}
