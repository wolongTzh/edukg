package com.tsinghua.edukg.service.utils;

import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.EntitySimp;
import com.tsinghua.edukg.model.EntityWithScore;
import com.tsinghua.edukg.model.VO.LinkingVO;

public class CombineServiceUtil {

    public static EntitySimp buildEntitySimpFromEntityWithScore(EntityWithScore entityWithScore) {
        if(entityWithScore == null) {
            return EntitySimp.builder().build();
        }
        EntitySimp entitySimp = EntitySimp.builder()
                .classList(entityWithScore.getClassList())
                .uri(entityWithScore.getUri())
                .abstractMsg(entityWithScore.getAbstractMsg())
                .name(entityWithScore.getName())
                .build();
        return entitySimp;
    }

    public static EntitySimp buildEntitySimpFromEntity(Entity entity) {
        if(entity == null) {
            return Entity.builder().build();
        }
        EntitySimp entitySimp = EntitySimp.builder()
                .classList(entity.getClassList())
                .uri(entity.getUri())
                .abstractMsg(entity.getAbstractMsg())
                .name(entity.getName())
                .build();
        return entitySimp;
    }

    public static EntitySimp buildEntitySimpFromLinkingVO(LinkingVO linkingVO) {
        if(linkingVO == null) {
            return Entity.builder().build();
        }
        EntitySimp entitySimp = EntitySimp.builder()
                .classList(linkingVO.getClassList())
                .uri(linkingVO.getUri())
                .abstractMsg(linkingVO.getAbstractMsg())
                .name(linkingVO.getName())
                .build();
        return entitySimp;
    }
}
