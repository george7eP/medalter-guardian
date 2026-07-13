package com.isoft.medalterguardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.medalterguardian.dao.SysLogMapper;
import com.isoft.medalterguardian.entity.SysLog;
import com.isoft.medalterguardian.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    /**
     * 每日凌晨 3 点自动清理 30 天前的操作日志，防止日志表无限膨胀
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanExpiredLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(SysLog::getCreateTime, cutoff);
        long deleted = baseMapper.delete(wrapper);
        if (deleted > 0) {
            log.info("日志自动清理完成，删除 {} 条 30 天前的记录", deleted);
        }
    }
}