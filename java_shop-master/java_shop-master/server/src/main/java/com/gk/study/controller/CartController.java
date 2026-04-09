package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Cart;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "购物车")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "加入购物车")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Transactional
    public APIResponse<Void> add(Cart cart) throws IOException {
        if (cart.getUserId() == null || cart.getUserId().isBlank()
                || cart.getThingId() == null || cart.getThingId().isBlank()) {
            return new APIResponse<>(ResponeCode.FAIL, "用户或商品不能为空");
        }
        cartService.addToCart(cart);
        return new APIResponse<>(ResponeCode.SUCCESS, "已加入购物车");
    }

    @Operation(summary = "修改数量")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/updateCount", method = RequestMethod.POST)
    @Transactional
    public APIResponse<Void> updateCount(String id, Integer itemCount) throws IOException {
        try {
            cartService.updateItemCount(id, itemCount == null ? 0 : itemCount);
        } catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponeCode.FAIL, e.getMessage());
        }
        return new APIResponse<>(ResponeCode.SUCCESS, "更新成功");
    }

    @Operation(summary = "删除购物车项")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @Transactional
    public APIResponse<Void> remove(String id) throws IOException {
        cartService.remove(id);
        return new APIResponse<>(ResponeCode.SUCCESS, "已删除");
    }

    @Operation(summary = "购物车列表")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse<List<Map<String, Object>>> list(String userId) throws IOException {
        return new APIResponse<>(ResponeCode.SUCCESS, "获取成功", cartService.listByUser(userId));
    }

    @Operation(summary = "清空购物车")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    @Transactional
    public APIResponse<Void> clear(String userId) throws IOException {
        cartService.clearByUser(userId);
        return new APIResponse<>(ResponeCode.SUCCESS, "已清空");
    }

    @Operation(summary = "购物车商品件数合计")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public APIResponse<Integer> count(String userId) throws IOException {
        return new APIResponse<>(ResponeCode.SUCCESS, "获取成功", cartService.sumItemCountByUser(userId));
    }
}
