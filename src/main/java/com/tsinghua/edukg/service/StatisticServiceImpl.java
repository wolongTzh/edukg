package com.tsinghua.edukg.service;

import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.GetEntityListParam;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.utils.RuleHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    RuleHandler ruleHandler;

    @Resource
    NeoManager neoManager;

    @Override
    public List<Entity> getEntityList(GetEntityListParam param) {
        String subject = param.getSubject();
        String label = param.getClassName();
        String keyWord = param.getKeyWord();
        Integer pageSize = param.getPageSize();
        Integer pageNo = param.getPageNo();
        subject = ruleHandler.convertSubjectZh2En(subject);
        subject = ruleHandler.convertSubject2Label(subject);
        List<String> uris = neoManager.getUrisFromKeyword(keyWord, subject, label, pageNo, pageSize);
        return neoManager.getEntityListFromUris(uris);
    }
}
