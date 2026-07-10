package com.isoft.medalterguardian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("warn_info")
public class WarnInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_id")
    private Long deviceId;

    @TableField("warn_level")
    private String warnLevel;

    @TableField("warn_content")
    private String warnContent;

    @TableField("warn_time")
    private LocalDateTime warnTime;

    @TableField("handle_status")
    private String handleStatus;

    @TableField("handle_user")
    private String handleUser;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField("handle_remark")
    private String handleRemark;
}
