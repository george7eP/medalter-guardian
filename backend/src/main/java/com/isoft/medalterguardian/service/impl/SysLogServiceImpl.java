package com.isoft.medalterguardian.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.medalterguardian.dao.SysLogMapper;
import com.isoft.medalterguardian.entity.SysLog;
import com.isoft.medalterguardian.service.SysLogService;
import org.springframework.stereotype.Service;
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {}