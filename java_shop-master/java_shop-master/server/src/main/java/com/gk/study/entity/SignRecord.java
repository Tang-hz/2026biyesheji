package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("b_sign_record")
public class SignRecord implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;

    @TableField("user_id")
    public Long userId;

    @TableField("sign_date")
    public String signDate;  // 签到日期，格式 yyyy-MM-dd

    @TableField("points")
    public Integer points;  // 获得积分数

    @TableField("create_time")
    public LocalDateTime createTime;
}
