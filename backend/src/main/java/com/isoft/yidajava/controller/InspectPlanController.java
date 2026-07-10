package com.isoft.yidajava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isoft.yidajava.common.Result;
import com.isoft.yidajava.entity.InspectPlan;
import com.isoft.yidajava.service.InspectPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.isoft.yidajava.annotation.Log;

@RestController
@RequestMapping("/inspect/plan")
public class InspectPlanController {

    @Autowired
    private InspectPlanService inspectPlanService;

    /**
     * 獲取檢修計畫分頁列表
     */
    @PreAuthorize("hasAuthority('inspect:plan')")
    @GetMapping
    public Result<Page<InspectPlan>> getPlanList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String planStatus) {

        Page<InspectPlan> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<InspectPlan> queryWrapper = new LambdaQueryWrapper<>();

        if (deviceId != null) {
            queryWrapper.eq(InspectPlan::getDeviceId, deviceId);
        }
        if (planStatus != null && !planStatus.isEmpty()) {
            queryWrapper.eq(InspectPlan::getPlanStatus, planStatus);
        }

        queryWrapper.orderByDesc(InspectPlan::getPlanDate);
        Page<InspectPlan> planPage = inspectPlanService.page(pageParam, queryWrapper);
        return Result.success(planPage);
    }

    /**
     * 新增檢修計畫
     */
    @PreAuthorize("hasAuthority('inspect:plan')")
    @Log("制定檢修計畫")
    @PostMapping
    public Result<InspectPlan> createPlan(@RequestBody InspectPlan plan) {
        boolean success = inspectPlanService.save(plan);
        if (success) {
            return Result.success("計畫新增成功", plan);
        } else {
            return Result.error("計畫新增失敗");
        }
    }

    /**
     * 修改檢修計畫
     */
    @PreAuthorize("hasAuthority('inspect:plan')")
    @Log("修改檢修計畫")
    @PutMapping("/{id}")
    public Result<InspectPlan> updatePlan(@PathVariable Long id, @RequestBody InspectPlan plan) {
        plan.setId(id);
        boolean success = inspectPlanService.updateById(plan);
        if (success) {
            return Result.success("計畫更新成功", plan);
        } else {
            return Result.error("計畫更新失敗");
        }
    }

    /**
     * 刪除檢修計畫
     */
    @PreAuthorize("hasAuthority('inspect:plan')")
    @Log("刪除檢修計畫")
    @DeleteMapping("/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        boolean success = inspectPlanService.removeById(id);
        if (success) {
            return Result.success("計畫刪除成功", null);
        } else {
            return Result.notFound("計畫不存在");
        }
    }
}