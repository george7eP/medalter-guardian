package com.isoft.medalterguardian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("device_info")
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_model")
    private String deviceModel;

    @TableField("manufacturer")
    private String manufacturer;

    @TableField("purchase_date")
    private LocalDate purchaseDate;

    @TableField("use_department")
    private String useDepartment;

    @TableField("inspect_cycle")
    private Integer inspectCycle;

    @TableField("warn_days")
    private Integer warnDays;

    @TableField("last_inspect_date")
    private LocalDate lastInspectDate;

    @TableField("device_status")
    private String deviceStatus;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
