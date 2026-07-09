package com.isoft.yidajava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.yidajava.dao.DeviceInfoMapper;
import com.isoft.yidajava.entity.DeviceInfo;
import com.isoft.yidajava.service.DeviceInfoService;
import org.springframework.stereotype.Service;

@Service
public class DeviceInfoServiceImpl extends ServiceImpl<DeviceInfoMapper, DeviceInfo> implements DeviceInfoService {
}
