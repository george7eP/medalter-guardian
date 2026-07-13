package com.isoft.medalterguardian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isoft.medalterguardian.common.Result;
import com.isoft.medalterguardian.entity.WarnRule;
import com.isoft.medalterguardian.service.WarnRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.isoft.medalterguardian.annotation.Log;

@RestController
@RequestMapping("/warn/rule")
public class WarnRuleController {

    @Autowired
    private WarnRuleService warnRuleService;

    /**
     * 獲取預警規則分頁列表
     */
    @PreAuthorize("hasAuthority('warn:rule')")
    @GetMapping
    public Result<Page<WarnRule>> getRuleList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String warnLevel,
            @RequestParam(required = false) Integer ruleStatus) {

        Page<WarnRule> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<WarnRule> queryWrapper = new LambdaQueryWrapper<>();

        if (deviceId != null) {
            queryWrapper.eq(WarnRule::getDeviceId, deviceId);
        }
        if (warnLevel != null && !warnLevel.isEmpty()) {
            queryWrapper.eq(WarnRule::getWarnLevel, warnLevel);
        }
        if (ruleStatus != null) {
            queryWrapper.eq(WarnRule::getRuleStatus, ruleStatus);
        }

        queryWrapper.orderByAsc(WarnRule::getId);
        Page<WarnRule> rulePage = warnRuleService.page(pageParam, queryWrapper);
        return Result.success(rulePage);
    }

    /**
     * 新增預警規則
     */
    @PreAuthorize("hasAuthority('warn:rule')")
    @Log("新增預警規則")
    @PostMapping
    public Result<WarnRule> createRule(@RequestBody WarnRule rule) {
        boolean success = warnRuleService.save(rule);
        if (success) {
            return Result.success("預警規則新增成功", rule);
        } else {
            return Result.error("預警規則新增失敗");
        }
    }

    /**
     * 修改預警規則
     */
    @PreAuthorize("hasAuthority('warn:rule')")
    @Log("修改預警規則")
    @PutMapping("/{id}")
    public Result<WarnRule> updateRule(@PathVariable Long id, @RequestBody WarnRule rule) {
        rule.setId(id);
        boolean success = warnRuleService.updateById(rule);
        if (success) {
            return Result.success("預警規則更新成功", rule);
        } else {
            return Result.error("預警規則更新失敗");
        }
    }

    /**
     * 刪除預警規則
     */
    @PreAuthorize("hasAuthority('warn:rule')")
    @Log("刪除預警規則")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        boolean success = warnRuleService.removeById(id);
        if (success) {
            return Result.success("預警規則刪除成功", null);
        } else {
            return Result.notFound("規則不存在");
        }
    }
}