package com.isoft.yidajava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.yidajava.dao.WarnInfoMapper;
import com.isoft.yidajava.entity.WarnInfo;
import com.isoft.yidajava.service.WarnInfoService;
import org.springframework.stereotype.Service;

@Service
public class WarnInfoServiceImpl extends ServiceImpl<WarnInfoMapper, WarnInfo> implements WarnInfoService {
}
