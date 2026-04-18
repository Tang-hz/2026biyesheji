package com.gk.study.service;


import com.gk.study.entity.Order;

import java.util.List;

public interface OrderService {
    List<Order> getOrderList();
    void createOrder(Order order, List<Order.ThingItem> items);
    void deleteOrder(String id);

    void updateOrder(Order order);

    List<Order> getUserOrderList(String userId, String status);
}
