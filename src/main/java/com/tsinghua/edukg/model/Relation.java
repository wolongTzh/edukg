package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 图谱关系
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Relation {

    String subject;

    String subjectUri;

    String object;

    String objectUri;

    String predicate;

    String predicateLabel;

    public Relation(Map<String, Object> map) {
        this.subject = (String) map.get("sourceName");
        this.subjectUri = (String) map.get("source");
        this.object = (String) map.get("targetName");
        this.objectUri = (String) map.get("target");
        this.predicate = (String) map.get("relation");
    }
}
