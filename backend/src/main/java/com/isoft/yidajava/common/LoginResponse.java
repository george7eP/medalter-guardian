package com.isoft.yidajava.common;

import com.isoft.yidajava.entity.SysPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private String username;

    private String realName;

    private List<SysPermission> permissions;
}
