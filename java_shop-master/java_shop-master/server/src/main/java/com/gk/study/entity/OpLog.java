package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("b_op_log")
public class OpLog implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    public Long id;
    @TableField
    public String reIp;
    @TableField("re_time")
    public String reTime;
    @TableField
    public String reUa;
    @TableField
    public String reUrl;
    @TableField
    public String reMethod;
    @TableField
    public String reContent;
    @TableField
    public String accessTime;

}
