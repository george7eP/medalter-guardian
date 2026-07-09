package com.isoft.yidajava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isoft.yidajava.common.Result;
import com.isoft.yidajava.entity.DeviceInfo;
import com.isoft.yidajava.service.DeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.isoft.yidajava.annotation.Log;

@RestController
@RequestMapping("/device")
public class DeviceInfoController {

    @Autowired
    private DeviceInfoService deviceInfoService;

    /**
     * 獲取設備分頁列表
     */
    @GetMapping
    public Result<Page<DeviceInfo>> getDeviceList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String deviceStatus) {

        Page<DeviceInfo> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<DeviceInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 模糊搜尋設備名稱
        if (deviceName != null && !deviceName.isEmpty()) {
            queryWrapper.like(DeviceInfo::getDeviceName, deviceName);
        }
        // 精確搜尋設備狀態 (NORMAL, WARN, FAULT)
        if (deviceStatus != null && !deviceStatus.isEmpty()) {
            queryWrapper.eq(DeviceInfo::getDeviceStatus, deviceStatus);
        }

        queryWrapper.orderByDesc(DeviceInfo::getCreateTime);
        Page<DeviceInfo> devicePage = deviceInfoService.page(pageParam, queryWrapper);
        return Result.success(devicePage);
    }

    /**
     * 新增設備
     */
    @Log("新增醫療設備")
    @PostMapping
    public Result<DeviceInfo> createDevice(@RequestBody DeviceInfo deviceInfo) {
        boolean success = deviceInfoService.save(deviceInfo);
        if (success) {
            return Result.success("設備添加成功", deviceInfo);
        } else {
            return Result.error("設備添加失敗");
        }
    }

    /**
     * 修改設備資訊
     */
    @Log("修改設備資訊")
    @PutMapping("/{id}")
    public Result<DeviceInfo> updateDevice(@PathVariable Long id, @RequestBody DeviceInfo deviceInfo) {
        DeviceInfo existing = deviceInfoService.getById(id);
        if (existing == null) {
            return Result.notFound("設備不存在");
        }
        deviceInfo.setId(id);
        boolean success = deviceInfoService.updateById(deviceInfo);
        if (success) {
            return Result.success("設備更新成功", deviceInfo);
        } else {
            return Result.error("設備更新失敗");
        }
    }

    /**
     * 刪除設備
     */
    @Log("刪除醫療設備")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        boolean success = deviceInfoService.removeById(id);
        if (success) {
            return Result.success("設備刪除成功", null);
        } else {
            return Result.notFound("設備不存在或刪除失敗");
        }
    }
}