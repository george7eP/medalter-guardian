package com.isoft.medalterguardian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inspect_record")
public class InspectRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_id")
    private Long deviceId;

    @TableField("inspect_date")
    private LocalDate inspectDate;

    @TableField("inspect_content")
    private String inspectContent;

    @TableField("inspect_result")
    private String inspectResult;

    @TableField("report_file")
    private String reportFile;

    @TableField("operator")
    private String operator;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
