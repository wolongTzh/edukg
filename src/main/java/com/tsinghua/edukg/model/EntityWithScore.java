package com.tsinghua.edukg.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 图谱实体排序版
 *
 * @author tanzheng
 * @date 2022/11/29
 */
@Data
@SuperBuilder(toBuilder = true)
public class EntityWithScore extends EntitySimp {

    int score;

    String k1;

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EntityWithScore) {
            EntityWithScore entityWithScore = (EntityWithScore) obj;
            if(entityWithScore.getUri().equals(this.getUri())) {
                return true;
            }
        }
        return false;
    }
}
