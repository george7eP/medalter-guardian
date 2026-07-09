package com.isoft.yidajava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("sys_permission")
public class SysPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("perm_code")
    private String permCode;

    @TableField("perm_name")
    private String permName;

    @TableField("perm_type")
    private String permType;

    @TableField("parent_id")
    private Long parentId;

    @TableField("path")
    private String path;

    @TableField("api_url")
    private String apiUrl;

    @TableField("sort")
    private Integer sort;

    @TableField("remark")
    private String remark;
}
