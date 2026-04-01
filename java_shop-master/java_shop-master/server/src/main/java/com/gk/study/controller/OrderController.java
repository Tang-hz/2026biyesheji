package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Order;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.OrderService;
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
 * 订单管理控制器
 * 负责处理订单相关的 HTTP 请求
 * 提供订单的查询、创建、删除、更新、取消等功能
 * 支持按用户和订单状态查询，区分管理员和普通用户操作权限
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    private final static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    OrderService service;

    /**
     * 获取订单列表
     * 查询系统中所有的订单信息（管理员功能）
     * 
     * @return APIResponse 包含订单列表的响应对象
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<Order> list =  service.getOrderList();

        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 获取用户订单列表
     * 根据用户 ID 和订单状态查询该用户的订单
     * 
     * @param userId 用户 ID
     * @param status 订单状态（1-未支付，2-已支付，7-已取消等）
     * @return APIResponse 包含订单列表的响应对象
     */
    @RequestMapping(value = "/userOrderList", method = RequestMethod.GET)
    public APIResponse userOrderList(String userId, String status){
        List<Order> list =  service.getUserOrderList(userId, status);
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    /**
     * 创建新订单
     * 
     * @param order 订单实体对象，包含订单详细信息
     * @return APIResponse 创建结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Order order) throws IOException {
        service.createOrder(order);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    /**
     * 删除订单
     * 需要管理员权限，支持批量删除
     * 
     * @param ids 要删除的订单 ID 字符串，多个 ID 用逗号分隔
     * @return APIResponse 删除结果响应
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteOrder(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    /**
     * 更新订单信息
     * 
     * @param order 订单实体对象，包含更新后的订单信息
     * @return APIResponse 更新结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Order order) throws IOException {
        service.updateOrder(order);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

    /**
     * 取消订单（管理员功能）
     * 将订单状态设置为已取消（状态码 7）
     * 
     * @param id 订单 ID
     * @return APIResponse 取消结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    @Transactional
    public APIResponse cancelOrder(Long id) throws IOException {
        Order order = new Order();
        order.setId(id);
        order.setStatus("7"); // 7=取消
        service.updateOrder(order);
        return new APIResponse(ResponeCode.SUCCESS, "取消成功");
    }

    /**
     * 取消用户订单（用户功能）
     * 登录用户可以取消自己的订单
     * 
     * @param id 订单 ID
     * @return APIResponse 取消结果响应
     * @throws IOException 可能抛出的 IO 异常
     */
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/cancelUserOrder", method = RequestMethod.POST)
    @Transactional
    public APIResponse cancelUserOrder(Long id) throws IOException {
        Order order = new Order();
        order.setId(id);
        order.setStatus("7"); // 7=取消
        service.updateOrder(order);
        return new APIResponse(ResponeCode.SUCCESS, "取消成功");
    }

}
