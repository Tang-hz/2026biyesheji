package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("b_cart")
public class Cart implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private String userId;
    @TableField("thing_id")
    private String thingId;
    /** 购买件数，对应列名 item_count，避免 SQL 保留字 count */
    @TableField("item_count")
    private Integer itemCount;
}
