package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@TableName("b_order")
public class Order implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    public Long id;
    @TableField
    public String status;
    @TableField
    public java.time.LocalDateTime orderTime;
    @TableField
    public java.time.LocalDateTime payTime;
    @TableField
    public String thingId;
    @TableField
    public String userId;
    @TableField
    public String count;
    @TableField
    public String orderNumber; // 订单编号
    @TableField
    public String receiverAddress;
    @TableField
    public String receiverName;
    @TableField
    public String receiverPhone;
    @TableField
    public String remark;
    @TableField("total_price")
    public BigDecimal totalPrice; // 订单总金额（含折扣）

    @TableField(exist = false)
    public String username; // 用户名
    @TableField(exist = false)
    public String title; // 商品名称
    @TableField(exist = false)
    public String cover; // 商品封面
    @TableField(exist = false)
    public String price; // 商品价格

    @TableField(exist = false)
    public Integer redeemPoints; // 积分抵扣数量（可选）

    // 订单商品项（用于批量创建订单）
    @Data
    public static class ThingItem implements Serializable {
        public String thingId;    // 商品ID
        public String count;     // 购买数量
        public String remark;    // 商品备注
    }
}
