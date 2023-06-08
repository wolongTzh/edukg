package com.tsinghua.edukg.manager;

import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.SourceAndCount;
import com.tsinghua.edukg.model.VO.UpdateTotalStatusVO;
import com.tsinghua.edukg.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * redis相关操作
 *
 * @author tanzheng
 * @date 2022/10/21
 */
@Component
public class RedisManager {

    @Autowired
    NeoAssisManager neoAssisManager;

    @Autowired
    RedisTemplateManager redisTemplateManager;

    @Autowired
    GraphService graphService;

    @Cacheable(value = ":statistic:maxId", key = "#subject", unless = "#result == null")
    public Integer getMaxIdWithSubject(String subject) {
        Integer maxId = neoAssisManager.getSubjectMaxId(subject);
        return maxId;
    }

    @Cacheable(value = ":statistic:topTen", key = "#subject", unless = "#result == null")
    public List<Entity> getHotEntities(String subject) {
        return neoAssisManager.getHotEntities(subject);
    }

    @CacheEvict(value = ":statistic:topTen", key = "#subject")
    public void deleteHotEntities(String subject) {
    }

    @Cacheable(value = ":statistic:updateSource", key = "'resource'", unless = "#result == null")
    public List<SourceAndCount> updateSource() {
        return neoAssisManager.updateSource();
    }

    @CacheEvict(value = ":statistic:updateSource", key = "'resource'")
    public void deleteUpdateSource() {
    }

    @Cacheable(value = ":statistic:updateTotalStatus", key = "'number'", unless = "#result == null")
    public UpdateTotalStatusVO updateTotalStatus() {
        return neoAssisManager.updateTotalStatus();
    }

    @CacheEvict(value = ":statistic:updateTotalStatus", key = "'number'")
    public void deleteUpdateTotalStatus() {
    }

    @Cacheable(value = ":statistic:updateSubjectStatus", key = "#subject", unless = "#result == null")
    public UpdateTotalStatusVO updateSubjectStatus(String subject) {
        return neoAssisManager.updateSubjectStatus(subject);
    }

    @Cacheable(value = ":subjectGraph", key = "#subject", unless = "#result == null")
    public List<Entity> getSubjectGraph(String subject) throws IOException {
        System.out.println("in getSubjectGraph redis");
        return graphService.getEntityFromSubjectInFile(subject);
    }

    @CacheEvict(value = ":statistic:updateSubjectStatus", key = "#subject")
    public void deleteUpdateSubjectStatus(String subject) {
    }

    public long subjectIdIncr(String subject, long idNum){
        String prefix = ":statistic:maxId::";
        return redisTemplateManager.incr(prefix + subject, idNum);
    }

}
