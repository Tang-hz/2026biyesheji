package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Ad;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.AdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 广告管理控制器
 * 负责处理广告相关的 HTTP 请求，包括广告的查询、创建、删除、更新等操作
 * 该控制器提供了完整的 CRUD 接口，支持图片上传功能
 */

@Tag(name = "广告管理控制层")
@RestController
@RequestMapping("/ad")
public class AdController {

    private final static Logger logger = LoggerFactory.getLogger(AdController.class);

    @Autowired
    AdService service;

    @Value("${File.uploadPath}")
    private String uploadPath;

    /**
     * 获取广告列表
     * 查询系统中所有的广告信息
     * 
     * @return APIResponse 包含广告列表的响应对象
     */
    @Operation(summary = "广告列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<Ad> list =  service.getAdList();
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 创建新广告
     * 需要管理员权限，支持上传广告图片
     * 图片将保存到配置的文件上传路径下
     * 
     * @param ad 广告实体对象，包含广告信息和图片文件
     * @return APIResponse 创建结果响应
     * @throws IOException 文件上传过程中可能抛出的 IO 异常
     */
    @Operation(summary = "新增广告")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Ad ad) throws IOException {
        if (!isValidHttpUrl(ad.getLink())) {
            return new APIResponse(ResponeCode.FAIL, "跳转链接必须以 http:// 或 https:// 开头");
        }
        String image = saveAd(ad);
        if(!StringUtils.isEmpty(image)) {
            ad.image = image;
        }

        service.createAd(ad);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除广告
     * 需要管理员权限，支持批量删除
     * 
     * @param ids 要删除的广告 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @Operation(summary = "批量删除广告")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteAd(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新广告信息
     * 需要管理员权限，支持更新广告图片
     * 
     * @param ad 广告实体对象，包含更新后的广告信息
     * @return APIResponse 更新结果响应
     * @throws IOException 文件上传过程中可能抛出的 IO 异常
     */
    @Operation(summary = "更新广告")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Ad ad) throws IOException {
        if (!isValidHttpUrl(ad.getLink())) {
            return new APIResponse(ResponeCode.FAIL, "跳转链接必须以 http:// 或 https:// 开头");
        }
        String image = saveAd(ad);
        if(!StringUtils.isEmpty(image)) {
            ad.image = image;
        }

        service.updateAd(ad);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

    /**
     * 保存广告图片
     * 处理上传的图片文件，生成随机文件名并保存到服务器
     * 
     * @param ad 广告实体对象，从中获取上传的图片文件
     * @return 保存后的新文件名，如果未上传文件则返回 null
     * @throws IOException 文件保存过程中可能抛出的 IO 异常
     */
    public String saveAd(Ad ad) throws IOException {
        MultipartFile file = ad.getImageFile();
        String newFileName = null;
        if(file !=null && !file.isEmpty()) {

            // 存文件
            String oldFileName = file.getOriginalFilename();
            String randomStr = UUID.randomUUID().toString();
            newFileName = randomStr + oldFileName.substring(oldFileName.lastIndexOf("."));
            String filePath = uploadPath + File.separator + "image" + File.separator + newFileName;
            File destFile = new File(filePath);
            if(!destFile.getParentFile().exists()){
                destFile.getParentFile().mkdirs();
            }
            file.transferTo(destFile);
        }
        if(!StringUtils.isEmpty(newFileName)) {
            ad.image = newFileName;
        }
        return newFileName;
    }

    private boolean isValidHttpUrl(String link) {
        if (StringUtils.isEmpty(link)) {
            return false;
        }
        String value = link.trim().toLowerCase();
        return value.startsWith("http://") || value.startsWith("https://");
    }
}
