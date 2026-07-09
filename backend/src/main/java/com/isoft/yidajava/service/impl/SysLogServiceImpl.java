package com.isoft.yidajava.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.yidajava.dao.SysLogMapper;
import com.isoft.yidajava.entity.SysLog;
import com.isoft.yidajava.service.SysLogService;
import org.springframework.stereotype.Service;
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {}