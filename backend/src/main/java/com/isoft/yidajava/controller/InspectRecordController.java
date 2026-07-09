package com.isoft.yidajava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isoft.yidajava.common.Result;
import com.isoft.yidajava.entity.InspectRecord;
import com.isoft.yidajava.service.InspectRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.isoft.yidajava.annotation.Log;

@RestController
@RequestMapping("/inspect/record")
public class InspectRecordController {

    @Autowired
    private InspectRecordService inspectRecordService;

    /**
     * 獲取檢修記錄分頁列表
     */
    @GetMapping
    public Result<Page<InspectRecord>> getRecordList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String inspectResult) {

        Page<InspectRecord> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<InspectRecord> queryWrapper = new LambdaQueryWrapper<>();

        if (deviceId != null) {
            queryWrapper.eq(InspectRecord::getDeviceId, deviceId);
        }
        // 精確搜尋結果 (PASS, PARTIAL, FAIL)
        if (inspectResult != null && !inspectResult.isEmpty()) {
            queryWrapper.eq(InspectRecord::getInspectResult, inspectResult);
        }

        queryWrapper.orderByDesc(InspectRecord::getInspectDate);
        Page<InspectRecord> recordPage = inspectRecordService.page(pageParam, queryWrapper);
        return Result.success(recordPage);
    }

    /**
     * 新增檢修記錄
     */
    @Log("提交檢修記錄")
    @PostMapping
    public Result<InspectRecord> createRecord(@RequestBody InspectRecord record) {
        boolean success = inspectRecordService.save(record);
        if (success) {
            return Result.success("記錄新增成功", record);
        } else {
            return Result.error("記錄新增失敗");
        }
    }

    /**
     * 修改檢修記錄
     */
    @Log("修改檢修記錄")
    @PutMapping("/{id}")
    public Result<InspectRecord> updateRecord(@PathVariable Long id, @RequestBody InspectRecord record) {
        record.setId(id);
        boolean success = inspectRecordService.updateById(record);
        if (success) {
            return Result.success("記錄更新成功", record);
        } else {
            return Result.error("記錄更新失敗");
        }
    }

    /**
     * 刪除檢修記錄
     */
    @Log("刪除檢修記錄")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        boolean success = inspectRecordService.removeById(id);
        if (success) {
            return Result.success("記錄刪除成功", null);
        } else {
            return Result.notFound("記錄不存在");
        }
    }
}