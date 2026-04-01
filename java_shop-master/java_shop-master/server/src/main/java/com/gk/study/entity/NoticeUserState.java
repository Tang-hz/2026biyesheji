package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("b_notice_user_state")
public class NoticeUserState implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("notice_id")
    private Long noticeId;

    @TableField("user_id")
    private Long userId;

    /**
     * 0=未删除, 1=已删除（仅对该用户生效）
     */
    @TableField("deleted")
    private Integer deleted;

    @TableField("delete_time")
    private String deleteTime;
}

