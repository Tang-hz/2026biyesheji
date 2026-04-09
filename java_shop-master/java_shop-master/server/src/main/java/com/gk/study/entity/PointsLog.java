package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("b_points_log")
public class PointsLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;

    @TableField("user_id")
    public Long userId;

    @TableField("points")
    public Integer points;  // 正数=获得，负数=消耗

    @TableField("type")
    public String type;  // ORDER/EVAL/SIGN/REDEEM

    @TableField("order_id")
    public Long orderId;

    @TableField("remark")
    public String remark;

    @TableField("create_time")
    public LocalDateTime createTime;
}
