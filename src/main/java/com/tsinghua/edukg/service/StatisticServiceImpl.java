package com.tsinghua.edukg.service;

import com.tsinghua.edukg.config.RedisConfig;
import com.tsinghua.edukg.manager.NeoAssisManager;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.manager.RedisManager;
import com.tsinghua.edukg.model.SourceAndCount;
import com.tsinghua.edukg.model.VO.UpdateTotalStatusVO;
import com.tsinghua.edukg.model.params.GetAllEntityListParam;
import com.tsinghua.edukg.model.params.GetEntityListParam;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.params.SubjectStatParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * statistic service impl
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Service
public class StatisticServiceImpl implements StatisticService {

    @Resource
    NeoManager neoManager;

    @Resource
    RedisManager redisManager;

    @Resource
    NeoAssisManager neoAssisManager;

    Integer openGate;

    public StatisticServiceImpl(RedisConfig redisConfig) {
        openGate = redisConfig.getOpenGate();
    }

    @Override
    public List<Entity> getEntityList(GetEntityListParam param) {
        String subject = param.getSubject();
        String label = param.getClassName();
        String keyWord = param.getKeyWord();
        Integer pageSize = param.getPageSize();
        Integer pageNo = param.getPageNo();
        List<String> uris = neoManager.getUrisFromKeywordWithPage(keyWord, subject, label, pageNo, pageSize);
        return neoManager.getEntityListFromUris(uris);
    }

    @Override
    public List<Entity> getAllEntityList(GetAllEntityListParam param) {
        String subject = param.getSubject();
        String label = param.getClassName();
        String keyWord = param.getKeyWord();
        List<String> uris = neoManager.getUrisFromKeyword(keyWord, subject, label);
        List<Entity> entities = neoManager.getEntityListFromUris(uris);
        return entities;
    }

    @Override
    public List<SourceAndCount> updateSource() {
        if(openGate == 0) {
            return neoAssisManager.updateSource();
        }
        return redisManager.updateSource();
    }

    @Override
    public UpdateTotalStatusVO updateTotalStatus() {
        if(openGate == 0) {
            return neoAssisManager.updateTotalStatus();
        }
        return redisManager.updateTotalStatus();
    }

    @Override
    public UpdateTotalStatusVO updateSubjectStatus(SubjectStatParam param) {
        if(openGate == 0) {
            return neoAssisManager.updateSubjectStatus(param.getSubject());
        }
        return redisManager.updateSubjectStatus(param.getSubject());
    }
}
