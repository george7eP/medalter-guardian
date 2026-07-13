package com.isoft.medalterguardian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isoft.medalterguardian.common.Result;
import com.isoft.medalterguardian.entity.WarnInfo;
import com.isoft.medalterguardian.service.WarnInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.isoft.medalterguardian.annotation.Log;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/warn/info")
public class WarnInfoController {

    @Autowired
    private WarnInfoService warnInfoService;

    /**
     * 獲取預警資訊分頁列表
     */
    @PreAuthorize("hasAuthority('warn:handle')")
    @GetMapping
    public Result<Page<WarnInfo>> getWarnList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String warnLevel,
            @RequestParam(required = false) String handleStatus,
            @RequestParam(required = false, defaultValue = "warnLevel") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {

        Page<WarnInfo> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<WarnInfo> queryWrapper = new LambdaQueryWrapper<>();

        if (deviceId != null) {
            queryWrapper.eq(WarnInfo::getDeviceId, deviceId);
        }
        if (warnLevel != null && !warnLevel.isEmpty()) {
            queryWrapper.eq(WarnInfo::getWarnLevel, warnLevel);
        }
        if (handleStatus != null && !handleStatus.isEmpty()) {
            queryWrapper.eq(WarnInfo::getHandleStatus, handleStatus);
        }

        applySort(queryWrapper, sortField, sortOrder);
        Page<WarnInfo> warnPage = warnInfoService.page(pageParam, queryWrapper);
        return Result.success(warnPage);
    }

    private void applySort(LambdaQueryWrapper<WarnInfo> qw, String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        switch (sortField) {
            case "warnTime":
                if (asc) qw.orderByAsc(WarnInfo::getWarnTime);
                else qw.orderByDesc(WarnInfo::getWarnTime);
                break;
            case "id":
                if (asc) qw.orderByAsc(WarnInfo::getId);
                else qw.orderByDesc(WarnInfo::getId);
                break;
            default: // warnLevel — 用 MySQL FIELD() 按嚴重程度排序
                String dir = asc ? "ASC" : "DESC";
                qw.last("ORDER BY FIELD(warn_level, 'LOW','MEDIUM','HIGH','URGENT') " + dir);
                break;
        }
    }

    /**
     * 處理預警資訊
     */
    @PreAuthorize("hasAuthority('warn:handle')")
    @Log("處理設備預警")
    @PutMapping("/handle/{id}")
    public Result<WarnInfo> handleWarn(@PathVariable Long id, @RequestBody WarnInfo warnInfo) {
        WarnInfo existing = warnInfoService.getById(id);
        if (existing == null) {
            return Result.notFound("預警資訊不存在");
        }

        // 寫入處理結果、處理人與處理備註，並自動標記處理時間
        existing.setHandleStatus(warnInfo.getHandleStatus());
        existing.setHandleUser(warnInfo.getHandleUser());
        existing.setHandleRemark(warnInfo.getHandleRemark());
        existing.setHandleTime(LocalDateTime.now());

        boolean success = warnInfoService.updateById(existing);
        if (success) {
            return Result.success("預警處理成功", existing);
        } else {
            return Result.error("預警處理失敗");
        }
    }

    /**
     * 刪除預警記錄
     */
    @PreAuthorize("hasAuthority('warn:handle')")
    @Log("刪除預警記錄")
    @DeleteMapping("/{id}")
    public Result<Void> deleteWarn(@PathVariable Long id) {
        boolean success = warnInfoService.removeById(id);
        if (success) {
            return Result.success("刪除成功", null);
        } else {
            return Result.notFound("記錄不存在");
        }
    }
}