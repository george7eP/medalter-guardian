package com.isoft.medalterguardian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isoft.medalterguardian.common.Result;
import com.isoft.medalterguardian.entity.SysLog;
import com.isoft.medalterguardian.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/log")
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    @PreAuthorize("hasAuthority('system:log')")
    @GetMapping
    public Result<Page<SysLog>> getLogList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username) {
        Page<SysLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysLog::getUsername, username);
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        return Result.success(sysLogService.page(pageParam, wrapper));
    }
}