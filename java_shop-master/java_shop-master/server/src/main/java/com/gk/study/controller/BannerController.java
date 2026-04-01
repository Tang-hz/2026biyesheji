package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Banner;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.BannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 轮播图管理控制器
 * 负责处理网站轮播图相关的 HTTP 请求
 * 提供轮播图的查询、创建、删除、更新等操作
 * 支持图片上传功能，仅管理员可操作
 */
@RestController
@RequestMapping("/banner")
public class BannerController {

    private final static Logger logger = LoggerFactory.getLogger(BannerController.class);

    @Autowired
    BannerService service;

    @Value("${File.uploadPath}")
    private String uploadPath;

    /**
     * 获取轮播图列表
     * 查询系统中所有的轮播图信息
     * 
     * @return APIResponse 包含轮播图列表的响应对象
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<Banner> list =  service.getBannerList();

        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 创建新轮播图
     * 需要管理员权限，支持上传轮播图图片
     * 
     * @param banner 轮播图实体对象，包含轮播图信息和图片文件
     * @return APIResponse 创建结果响应
     * @throws IOException 文件上传过程中可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Banner banner) throws IOException {
        String image = saveBanner(banner);
        if(!StringUtils.isEmpty(image)) {
            banner.image = image;
        }

        service.createBanner(banner);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除轮播图
     * 需要管理员权限，支持批量删除
     * 
     * @param ids 要删除的轮播图 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteBanner(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新轮播图信息
     * 需要管理员权限，支持更新轮播图图片
     * 
     * @param banner 轮播图实体对象，包含更新后的轮播图信息
     * @return APIResponse 更新结果响应
     * @throws IOException 文件上传过程中可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Banner banner) throws IOException {
        String image = saveBanner(banner);
        if(!StringUtils.isEmpty(image)) {
            banner.image = image;
        }

        service.updateBanner(banner);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

    /**
     * 保存轮播图图片
     * 处理上传的图片文件，生成随机文件名并保存到服务器
     * 
     * @param banner 轮播图实体对象，从中获取上传的图片文件
     * @return 保存后的新文件名，如果未上传文件则返回 null
     * @throws IOException 文件保存过程中可能抛出的 IO 异常
     */
    public String saveBanner(Banner banner) throws IOException {
        MultipartFile file = banner.getImageFile();
        String newFileName = null;
        if(file !=null && !file.isEmpty()) {

            // 存文件
            String oldFileName = file.getOriginalFilename();
            String randomStr = UUID.randomUUID().toString();
            newFileName = randomStr + oldFileName.substring(oldFileName.lastIndexOf("."));
            String filePath = uploadPath + File.separator + "banner" + File.separator + newFileName;
            File destFile = new File(filePath);
            if(!destFile.getParentFile().exists()){
                destFile.getParentFile().mkdirs();
            }
            file.transferTo(destFile);
        }
        if(!StringUtils.isEmpty(newFileName)) {
            banner.image = newFileName;
        }
        return newFileName;
    }
}
