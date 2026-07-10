package com.isoft.medalterguardian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.medalterguardian.dao.WarnInfoMapper;
import com.isoft.medalterguardian.entity.WarnInfo;
import com.isoft.medalterguardian.service.WarnInfoService;
import org.springframework.stereotype.Service;

@Service
public class WarnInfoServiceImpl extends ServiceImpl<WarnInfoMapper, WarnInfo> implements WarnInfoService {
}
