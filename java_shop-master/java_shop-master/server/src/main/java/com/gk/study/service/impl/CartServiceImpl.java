package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.entity.Cart;
import com.gk.study.mapper.CartMapper;
import com.gk.study.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Override
    public void addToCart(Cart in) {
        int n = in.getItemCount() == null || in.getItemCount() < 1 ? 1 : in.getItemCount();
        QueryWrapper<Cart> qw = new QueryWrapper<>();
        qw.eq("user_id", in.getUserId()).eq("thing_id", in.getThingId());
        Cart existing = cartMapper.selectOne(qw);
        if (existing != null) {
            existing.setItemCount(existing.getItemCount() + n);
            cartMapper.updateById(existing);
        } else {
            Cart c = new Cart();
            c.setUserId(in.getUserId());
            c.setThingId(in.getThingId());
            c.setItemCount(n);
            cartMapper.insert(c);
        }
    }

    @Override
    public void updateItemCount(String id, int itemCount) {
        if (itemCount < 1) {
            throw new IllegalArgumentException("数量至少为 1");
        }
        Cart c = cartMapper.selectById(id);
        if (c == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        c.setItemCount(itemCount);
        cartMapper.updateById(c);
    }

    @Override
    public void remove(String id) {
        cartMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> listByUser(String userId) {
        return cartMapper.getCartList(userId);
    }

    @Override
    public void clearByUser(String userId) {
        QueryWrapper<Cart> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        cartMapper.delete(qw);
    }

    @Override
    public int sumItemCountByUser(String userId) {
        Long n = cartMapper.sumItemCountByUserId(userId);
        return n == null ? 0 : n.intValue();
    }
}
