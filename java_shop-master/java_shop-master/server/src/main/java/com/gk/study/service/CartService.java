package com.gk.study.service;

import com.gk.study.entity.Cart;

import java.util.List;
import java.util.Map;

public interface CartService {

    void addToCart(Cart cart);

    void updateItemCount(String id, int itemCount);

    void remove(String id);

    List<Map<String, Object>> listByUser(String userId);

    void clearByUser(String userId);

    int sumItemCountByUser(String userId);
}
