package com.tsinghua.edukg.manager;

import com.tsinghua.edukg.constant.WebConstant;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.model.*;
import com.tsinghua.edukg.model.VO.UpdateTotalStatusVO;
import com.tsinghua.edukg.utils.RuleHandler;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * neo4j相关辅助操作（用于在redis manager中调用）
 *
 * @author tanzheng
 * @date 2022/10/21
 */

@Component
public class NeoAssisManager {

    @Autowired
    @Qualifier("neo4jSession")
    Session session;

    public Integer getSubjectMaxId(String subject) {
        String label = RuleHandler.convertSubject2Label(subject);
        String subjectTemplate = RuleHandler.convertLabel2UriTemplate(subject);
        Pattern idRe = RuleHandler.findSubjectUriId(subject);
        String query = "MATCH (N:`Resource`:`" + label + "`) WHERE N.uri STARTS WITH $subjectTemplate RETURN COUNT(N) as count";
        Map<String, Object> map = new HashMap<>();
        map.put("subjectTemplate", subjectTemplate);
        Result result = session.query(query, map);
        Integer length = 0;
        String countPre = "";
        for (Map<String, Object> m : result.queryResults()) {
            length = (subjectTemplate + String.valueOf(((Number) m.get("count")).intValue() - 1)).length();
            countPre = String.valueOf(m.get("count"));
        }
        query = "MATCH (N:`Resource`:`" + label + "`) " +
                "WHERE N.uri STARTS WITH $subjectTemplate AND SIZE(N.uri) = $length " +
                "RETURN MAX(N.uri) as maxUri";
        map.put("length", length);
        result = session.query(query, map);
        String uri = "";
        for (Map<String, Object> m : result.queryResults()) {
            if(m.size() == 0) {
                throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "没有查到最大id");
            }
            uri = (String) m.get("maxUri");
        }
        Integer maxId = null;
        try {
            Matcher matcher = idRe.matcher(uri);
            if(matcher.find()) {
                maxId = Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            System.out.println("uri = " + uri);
            System.out.println("subjectTemplate = " + subjectTemplate);
            System.out.println("countPre = " + countPre);
            System.out.println("length = " + length);
            e.printStackTrace();
        }
        return maxId;
    }

    public List<Entity> getHotEntities(String subject) {
        List<Entity> entities = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("subject", subject);
        String query = "MATCH (n: `Resource`: `" + subject + "`)-[e]-() " +
                "WHERE n.uri =~ \".*#main-E.*\" " +
                "RETURN n.uri AS uri, n.rdfs__label AS name, COUNT(e) AS count ORDER BY COUNT(e) DESC LIMIT 10";
        Result result = session.query(query, map);
        for (Map<String, Object> m : result.queryResults()) {
            entities.add(Entity.builder()
                    .uri((String) m.get("uri"))
                    .name((String) m.get("name")).build()
            );
        }
        return entities;
    }

    public List<SourceAndCount> updateSource() {
        List<SourceAndCount> sourceAndCountList = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        String query = "MATCH (n:`Resource`) RETURN n.uri as uri";
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            String uri = (String) m.get("uri");
            Matcher matcher = RuleHandler.findSourceRe(uri);
            String key = "unkwown";
            if(matcher.find()) {
                key = matcher.group(1);
            }
            Integer count = map.getOrDefault(key, -1);
            map.put(key, ++count);
        }
        for(Map.Entry entry : map.entrySet()) {
            sourceAndCountList.add(new SourceAndCount((String) entry.getKey(), (Integer) entry.getValue()));
        }
        return sourceAndCountList;
    }

    public UpdateTotalStatusVO updateTotalStatus() {
        String query = "MATCH (n:`Resource`) RETURN COUNT(n) as count";
        Result result = session.query(query, new HashMap<>());
        Integer entities = 0;
        for (Map<String, Object> m : result.queryResults()) {
            entities = ((Number) m.get("count")).intValue();
        }
        query = "MATCH (n:`Resource`) " +
                "WITH LABELS(n) AS labels " +
                "UNWIND labels AS label " +
                "RETURN DISTINCT(label) as labelName, COUNT(label) as count";
        result = session.query(query, new HashMap<>());
        Map<String, Integer> rawLabelCount = new HashMap<>();
        for (Map<String, Object> m : result.queryResults()) {
            String key = (String) m.get("labelName");
            Integer value = ((Number) m.get("count")).intValue();
            Matcher subjectLabelMatcher = RuleHandler.findSubjectLabel(key);
            Matcher labelAbbrMatcher = RuleHandler.findLabelAbbr(key);
            if(labelAbbrMatcher.find() && !subjectLabelMatcher.find()) {
                rawLabelCount.put(key, value);
            }
        }
        query = "MATCH ()-[r]->() RETURN COUNT(r) as count";
        Integer relations = 0;
        result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            relations = ((Number) m.get("count")).intValue();
        }
        query = "MATCH (n:`Resource`) UNWIND KEYS(n) AS KEY RETURN COUNT(KEY) as count";
        result = session.query(query, new HashMap<>());
        Integer properties = 0;
        for (Map<String, Object> m : result.queryResults()) {
            properties = ((Number) m.get("count")).intValue();
        }
        Map<String, List<String>> subOfAbbrMap = RuleHandler.grepSubClassOfAbbrMap();
        Map<String, Integer> labelCount = new HashMap<>();
        for(Map.Entry entry : subOfAbbrMap.entrySet()) {
            String label = (String) entry.getKey();
            List<String> uriList = (List<String>) entry.getValue();
            Integer acc = 0;
            for(String uri : uriList) {
                acc += rawLabelCount.getOrDefault(uri, 0);
            }
            if(acc != 0) {
                labelCount.put(label, acc);
            }
        }
        for(Map.Entry entry : rawLabelCount.entrySet()) {
            String label = (String) entry.getKey();
            if(!labelCount.containsKey(label)) {
                labelCount.put(label, rawLabelCount.get(label));
            }
        }
        Integer classes = labelCount.size();
        Integer classesGt50 = labelCount.entrySet().stream().filter(n -> n.getValue() >= 50).toArray().length;
        return new UpdateTotalStatusVO(classes, classesGt50, entities, relations, properties);
    }

    public UpdateTotalStatusVO updateSubjectStatus(String subject) {
        List<String> labels = new ArrayList<>();
        labels.add("Resource");
        if(!subject.equals("total")) {
            labels.add(RuleHandler.convertSubject2Label(subject));
        }
        String labelStr = "`" + String.join("`:`", labels) + "`";
        String query = String.format("MATCH (n:%s) RETURN COUNT(n) as count", labelStr);
        Result result = session.query(query, new HashMap<>());
        Integer entities = 0;
        for (Map<String, Object> m : result.queryResults()) {
            entities = ((Number) m.get("count")).intValue();
        }
        query = String.format("MATCH (n:%s) WITH LABELS(n) AS labels UNWIND labels AS label " +
                "RETURN DISTINCT(label) as labelName, COUNT(label) as count", labelStr);
        result = session.query(query, new HashMap<>());
        Map<String, Integer> rawLabelCount = new HashMap<>();
        for (Map<String, Object> m : result.queryResults()) {
            String key = (String) m.get("labelName");
            Integer value = ((Number) m.get("count")).intValue();
            Matcher subjectLabelMatcher = RuleHandler.findSubjectLabel(key);
            Matcher labelAbbrMatcher = RuleHandler.findLabelAbbr(key);
            if(labelAbbrMatcher.find() && !subjectLabelMatcher.find()) {
                rawLabelCount.put(key, value);
            }
        }
        query = String.format("MATCH (n:%s)-[r]-() RETURN COUNT(DISTINCT r) as count", labelStr);
        Integer relations = 0;
        result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            relations = ((Number) m.get("count")).intValue();
        }
        query = String.format("MATCH (n:%s) UNWIND KEYS(n) AS KEY RETURN COUNT(KEY) as count", labelStr);
        result = session.query(query, new HashMap<>());
        Integer properties = 0;
        for (Map<String, Object> m : result.queryResults()) {
            properties = ((Number) m.get("count")).intValue();
        }
        Map<String, List<String>> subOfAbbrMap = RuleHandler.grepSubClassOfAbbrMap();
        Map<String, Integer> labelCount = new HashMap<>();
        for(Map.Entry entry : subOfAbbrMap.entrySet()) {
            String label = (String) entry.getKey();
            List<String> uriList = (List<String>) entry.getValue();
            Integer acc = 0;
            for(String uri : uriList) {
                acc += rawLabelCount.getOrDefault(uri, 0);
            }
            if(acc != 0) {
                labelCount.put(label, acc);
            }
        }
        for(Map.Entry entry : rawLabelCount.entrySet()) {
            String label = (String) entry.getKey();
            if(!labelCount.containsKey(label)) {
                labelCount.put(label, rawLabelCount.get(label));
            }
        }
        Integer classes = labelCount.size();
        Integer classesGt50 = labelCount.entrySet().stream().filter(n -> n.getValue() >= 50).toArray().length;
        return new UpdateTotalStatusVO(classes, classesGt50, entities, relations, properties);
    }
}
