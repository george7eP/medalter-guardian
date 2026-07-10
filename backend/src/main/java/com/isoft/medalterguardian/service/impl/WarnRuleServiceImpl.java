package com.isoft.medalterguardian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.medalterguardian.dao.WarnRuleMapper;
import com.isoft.medalterguardian.entity.WarnRule;
import com.isoft.medalterguardian.service.WarnRuleService;
import org.springframework.stereotype.Service;

@Service
public class WarnRuleServiceImpl extends ServiceImpl<WarnRuleMapper, WarnRule> implements WarnRuleService {
}
