package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Address;
import com.gk.study.service.AddressService;
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
 * 地址管理控制器
 * 负责处理用户收货地址相关的 HTTP 请求
 * 提供地址的查询、创建、删除、更新等功能
 * 支持设置默认地址，当设置某个地址为默认时会自动将其他地址设为非默认
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    private final static Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    AddressService service;

    /**
     * 获取用户地址列表
     * 根据用户 ID 查询该用户的所有收货地址
     * 
     * @param userId 用户 ID
     * @return APIResponse 包含地址列表的响应对象
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(String userId){
        List<Address> list =  service.getAddressList(userId);
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 创建新地址
     * 如果设置为默认地址，会自动将该用户的其他地址设为非默认
     * 
     * @param address 地址实体对象，包含地址信息
     * @return APIResponse 创建结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Address address) throws IOException {
        if(address.getDef().equals("1")){
            // 其它置 0
            List<Address> list =  service.getAddressList(address.getUserId());
            for(Address address1: list) {
                address1.setDef("0");
                service.updateAddress(address1);
            }
        }
        service.createAddress(address);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除地址
     * 支持批量删除地址
     * 
     * @param ids 要删除的地址 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteAddress(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新地址信息
     * 如果设置为默认地址，会自动将该用户的其他地址设为非默认
     * 
     * @param address 地址实体对象，包含更新后的地址信息
     * @return APIResponse 更新结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Address address) throws IOException {
        if(address.getDef().equals("1")){
            // 其它置 0
            List<Address> list =  service.getAddressList(address.getUserId());
            for(Address address1: list) {
                address1.setDef("0");
                service.updateAddress(address1);
            }
        }
        service.updateAddress(address);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

}
