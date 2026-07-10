package com.isoft.medalterguardian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.medalterguardian.dao.DeviceInfoMapper;
import com.isoft.medalterguardian.entity.DeviceInfo;
import com.isoft.medalterguardian.service.DeviceInfoService;
import org.springframework.stereotype.Service;

@Service
public class DeviceInfoServiceImpl extends ServiceImpl<DeviceInfoMapper, DeviceInfo> implements DeviceInfoService {
}
