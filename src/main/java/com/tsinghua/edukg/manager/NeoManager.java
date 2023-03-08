package com.tsinghua.edukg.manager;

import com.tsinghua.edukg.constant.BusinessConstant;
import com.tsinghua.edukg.constant.WebConstant;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.model.*;
import com.tsinghua.edukg.model.VO.QuikAddEntitiesVO;
import com.tsinghua.edukg.utils.RuleHandler;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * neo4j相关语句操作
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Component
public class NeoManager {

    @Autowired
    @Qualifier("neo4jSession")
    Session session;

    @Autowired
    RedisManager redisManager;

    public List<String> getUrisFromKeywordWithPage(String keyword, String subject, String label, Integer page, Integer size) {
        String query = "MATCH (n:Resource) %s RETURN n.uri skip $skip LIMIT $limit";
        Map<String, Object> map = new HashMap<>();
        map.put("skip", (page * size));
        map.put("limit", size);
        List<String> conditions = new ArrayList<>();
        if(!StringUtils.isEmpty(subject)) {
            conditions.add("n:`" + subject + "`");
        }
        if(!StringUtils.isEmpty(label)) {
            label = label.replace("'", "");
            conditions.add("n:`" + label + "`");
        }
        if(!StringUtils.isEmpty(keyword)) {
            keyword = keyword.replace(".", "").replace("*", "").replace("\"", "");
            conditions.add("n.uri=~ \".*" + keyword + ".*\"");
        }
        String params = "";
        if(conditions.size() != 0) {
            params = " WHERE " + String.join(" AND ", conditions);
        }
        query = String.format(query, params);
        List<String> retList = new ArrayList<>();
        Result result = session.query(query, map);
        for (Map<String, Object> m : result.queryResults()) {
            for(Map.Entry<String, Object> entry : m.entrySet()) {
                retList.add((String) entry.getValue());
            }
        }
        return retList;
    }

    public List<String> getUrisFromKeyword(String keyword, String subject, String label) {
        String query = "MATCH (n:Resource) %s RETURN n.uri LIMIT $limit";
        Map<String, Object> map = new HashMap<>();
        List<String> conditions = new ArrayList<>();
        if(!StringUtils.isEmpty(subject)) {
            conditions.add("n:`" + subject + "`");
        }
        if(!StringUtils.isEmpty(label)) {
            label = label.replace("'", "");
            conditions.add("n:`" + label + "`");
        }
        if(!StringUtils.isEmpty(keyword)) {
            keyword = keyword.replace(".", "").replace("*", "").replace("\"", "");
            conditions.add("n.uri=~ \".*" + keyword + ".*\"");
        }
        String params = "";
        if(conditions.size() != 0) {
            params = " WHERE " + String.join(" AND ", conditions);
        }
        query = String.format(query, params);
        List<String> retList = new ArrayList<>();
        map.put("limit", BusinessConstant.SEARCH_MAX_LENGTH);
        Result result = session.query(query, map);
        for (Map<String, Object> m : result.queryResults()) {
            for(Map.Entry<String, Object> entry : m.entrySet()) {
                retList.add((String) entry.getValue());
            }
        }
        return retList;
    }

    public List<Entity> getEntityListFromUris(List<String> uriList) {
        String query = "MATCH (n:Resource) WHERE n.uri IN $uris RETURN n.uri as uri, n.rdfs__label as rdfs__label, labels(n) as labels, properties(n) as properties";
        Map<String, Object> map = new HashMap<>();
        map.put("uris", uriList);
        Result result = session.query(query, map);
        List<Entity> entities = new ArrayList<>();

        for (Map<String, Object> m : result.queryResults()) {
            Map<String, String> rawProperty = (Map<String, String>) m.get("properties");
            Map<String, String> newProperty = new HashMap<>();
            for(Map.Entry entry : rawProperty.entrySet()) {
                String key = (String) entry.getKey();
                if(!key.equals("rdfs__label") && !key.equals("uri")) {
                    newProperty.put(RuleHandler.getPropertyNameByAbbr(key), (String) entry.getValue());
                }
            }
            List<Property> properties = new ArrayList<>();
            String entityName = (String) m.get("rdfs__label");
            for(Map.Entry entry : newProperty.entrySet()) {
                properties.add(Property.builder()
                        .predicate((String) entry.getKey())
                        .object((String) entry.getValue())
                        .subject(entityName)
                        .build());
            }
            RuleHandler.propertyConverter(properties);
            entities.add(Entity.builder()
                    .abstractMsg("")
                    .name((String) m.get("rdfs__label"))
                    .uri((String) m.get("uri"))
                    .property(properties)
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels")))).build());
        }
        return entities;
    }

    public List<Entity> getEntityListFromName(String name) {
        List<Entity> entityList = new ArrayList<>();
        String query = "MATCH (n:`Resource`) WHERE n.rdfs__label = \"%s\" RETURN n.uri as uri, n.rdfs__label as rdfs__label, labels(n) as labels";
        query = String.format(query, name);
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            entityList.add(Entity.builder()
                    .abstractMsg("")
                    .name((String) m.get("rdfs__label"))
                    .uri((String) m.get("uri"))
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels")))).build());
        }
        return entityList;
    }

    public List<EntityWithScore> getEntityWithScoreFromProperty(String name) {
        List<EntityWithScore> retList = new ArrayList<>();
        String query = "match(n:Resource)" +
                " where any(k in keys(n) where n[k] contains \"%s\")" +
                " with n, keys(n) as ks" +
                " unwind ks as k1" +
                " match(n)" +
                " where n[k1] contains \"%s\" and k1 starts with \"edukg\"" +
                " return n.rdfs__label as name, n.uri as uri, labels(n) as labels, n[k1] as v" +
                " limit 10";
        query = String.format(query, name, name);
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            retList.add(EntityWithScore.builder()
                    .name((String) m.get("name"))
                    .uri((String) m.get("uri"))
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels"))))
                    .abstractMsg("")
                    .score(scoreCalculator((String) m.get("v"), name, false))
                    .build());
        }
        retList = retList.stream().sorted(Comparator.comparingInt(EntityWithScore::getScore)).collect(Collectors.toList());
        return retList.stream().distinct().collect(Collectors.toList());
    }

    public List<EntityWithScore> getEntityWithScoreFromName(String name) {
        List<EntityWithScore> retList = new ArrayList<>();
        String query = "match(n:Resource)" +
                " where n.rdfs__label contains \"%s\"" +
                " return n.rdfs__label as name, n.uri as uri, labels(n) as labels" +
                " limit 10";
        query = String.format(query, name, name);
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            retList.add(EntityWithScore.builder()
                    .name((String) m.get("name"))
                    .uri((String) m.get("uri"))
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels"))))
                    .abstractMsg("")
                    .score(scoreCalculator((String) m.get("name"), name, true))
                    .build());
        }
        retList = retList.stream().sorted(Comparator.comparingInt(EntityWithScore::getScore)).collect(Collectors.toList());
        return retList.stream().distinct().collect(Collectors.toList());
    }

    public Entity getBareEntityFromUri(String uri) {
        String query = "MATCH (n:`Resource`) WHERE n.uri = $uri RETURN n.uri as uri, n.rdfs__label as rdfs__label, labels(n) as labels, properties(n) as properties";
        Map<String, Object> map = new HashMap<>();
        map.put("uri", uri);
        Result result = session.query(query, map);
        Entity entity = Entity.builder().build();
        for (Map<String, Object> m : result.queryResults()) {
            List<Property> properties = new ArrayList<>();
            Map<String, String> propertyRaw = (Map<String, String>) m.get("properties");
            String entityName = (String) m.get("rdfs__label");
            for(Map.Entry entry : propertyRaw.entrySet()) {
                properties.add(Property.builder()
                        .predicate((String) entry.getKey())
                        .object((String) entry.getValue())
                        .subject(entityName)
                        .build());
            }
            RuleHandler.propertyConverter(properties);
            entity = Entity.builder()
                    .abstractMsg("")
                    .name((String) m.get("rdfs__label"))
                    .uri((String) m.get("uri"))
                    .property(properties)
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels")))).build();
        }
        return entity;
    }

    public Entity getEntityFromUri(String uri) {
        List<Relation> relations = new ArrayList<>();
        Entity entity = getBareEntityFromUri(uri);
        if(entity.getUri() == null) {
            return entity;
        }
        String tailQuery = " MATCH (n:`Resource` { uri: $uri})<-[e]-(m) " +
                "RETURN m.uri AS source, m.rdfs__label AS sourceName, " +
                "n.uri AS target, n.rdfs__label AS targetName, " +
                "TYPE(e) AS relation";
        Map<String, Object> map = new HashMap<>();
        map.put("uri", uri);
        Result result = session.query(tailQuery, map);
        for (Map<String, Object> m : result.queryResults()) {
            Relation relation = new Relation(m);
            relations.add(relation);
        }
        String headQuery = " MATCH (n:`Resource` { uri: $uri})-[e]->(m) " +
                "RETURN n.uri AS source, n.rdfs__label AS sourceName, " +
                "m.uri AS target, m.rdfs__label AS targetName, " +
                "TYPE(e) AS relation";
        result = session.query(headQuery, map);
        for (Map<String, Object> m : result.queryResults()) {
            Relation relation = new Relation(m);
            relations.add(relation);
        }
        RuleHandler.relationConverter(relations);
        entity.setRelation(relations);
        return entity;
    }

    public List<Relation> findPathBetweenNodesOld(String head, String tail) {
        Entity headNode = getBareEntityFromUri(head);
        Entity tailNode = getBareEntityFromUri(tail);
        if(headNode == null || tailNode == null) {
            return null;
        }
        String findNodesInPathQuery = "MATCH p=(n1:`Resource` { uri: $headUri}) " +
                "-[*.." + BusinessConstant.SEARCH_MAX_LENGTH + "] " +
                "-> (n2:`Resource` { uri: $tailUri}) " +
                "UNWIND NODES(p) AS n " +
                "RETURN DISTINCT n.uri as uri";
        Map<String, Object> map = new HashMap<>();
        map.put("headUri", head);
        map.put("tailUri", tail);
        Result result = session.query(findNodesInPathQuery, map);
        List<String> uris = new ArrayList<>();
        for (Map<String, Object> m : result.queryResults()) {
            uris.add((String) m.get("uri"));
        }
        if(uris.size() == 0) {
            return null;
        }
        List<Relation> relations = new ArrayList<>();
        String targetAsTailQuery = "MATCH (n:`Resource`)<-[e]-(m) WHERE n.uri in $uris " +
                "RETURN m.uri AS source, m.rdfs__label AS sourceName, " +
                "n.uri AS target, n.rdfs__label AS targetName, " +
                "TYPE(e) AS relation";
        map = new HashMap<>();
        map.put("uris", uris);
        result = session.query(targetAsTailQuery, map);
        for (Map<String, Object> m : result.queryResults()) {
            relations.add(new Relation(m));
        }
        String targetAsHeadQuery = "MATCH (n:`Resource`)-[e]->(m) WHERE n.uri in $uris " +
                "RETURN n.uri AS source, n.rdfs__label AS sourceName, " +
                "m.uri AS target, m.rdfs__label AS targetName, " +
                "TYPE(e) AS relation";
        result = session.query(targetAsHeadQuery, map);
        for (Map<String, Object> m : result.queryResults()) {
            relations.add(new Relation(m));
        }
        RuleHandler.relationConverter(relations);
        return relations;
    }

    public List<Relation> findPathBetweenNodes(String head, String tail, Integer maxJumpTime) {
        Entity headNode = getBareEntityFromUri(head);
        Entity tailNode = getBareEntityFromUri(tail);
        if(headNode == null || tailNode == null) {
            return null;
        }
        String findNodesInPathQuery = "match l=allshortestpaths((" +
                "x{uri:'" + head + "'})" +
                "-[*.." + maxJumpTime + "]-(y{uri:'" + tail + "'}))" +
                " with *, relationships(l) as rels" +
                " where all(rel in rels where type(rel) <> \"edukg_prop_common__main-R1\"" +
                " and type(rel) <> \"edukg_prop_common__main-R3\"" +
                " and type(rel) <> \"edukg_prop_common__main-R10\")" +
                " UNWIND relationships(l) as r UNWIND NODES(l) as n return r,n";
        Result result = session.query(findNodesInPathQuery, new HashMap<>());
        Map<String, String> relationMap = new HashMap<>();
        Map<String, NodeModel> nodeMap = new HashMap<>();
        String splitIdTag = "-";
        for (Map<String, Object> m : result.queryResults()) {
            RelationshipModel relation = (RelationshipModel) m.get("r");
            NodeModel node = (NodeModel) m.get("n");
            String relationKey = relation.getStartNode().toString() + splitIdTag + relation.getEndNode().toString();
            if(!relationMap.containsKey(relationKey)) {
                relationMap.put(relationKey, relation.getType());
            }
            nodeMap.put(node.getId().toString(), node);
        }
        List<Relation> relations = new ArrayList<>();
        for(Map.Entry entry : relationMap.entrySet()) {
            String type = (String) entry.getValue();
            String startId = ((String) entry.getKey()).split("-")[0];
            String endId = ((String) entry.getKey()).split("-")[1];
            NodeModel start = nodeMap.get(startId);
            NodeModel end = nodeMap.get(endId);
            Relation relation = Relation.builder()
                    .subject((String) start.property("rdfs__label"))
                    .subjectUri((String) start.property("uri"))
                    .object((String) end.property("rdfs__label"))
                    .objectUri((String) end.property("uri"))
                    .predicate(type)
                    .build();
            relations.add(relation);
        }
        RuleHandler.relationConverter(relations);
        return relations;
    }

    public void updateProperty(String uri, Property prev, Property next) {
//        String subject = RuleHandler.getSubjectByUri(uri);
//        if(subject == null) {
//            throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "uri格式不准确");
//        }
        String query = "MATCH (n:`Resource` { `uri`: $uri }) RETURN n.`rdfs__label` as name";
        Map<String, Object> map = new HashMap<>();
        map.put("uri", uri);
        Result result = session.query(query, map);
        String name = "";
        for (Map<String, Object> m : result.queryResults()) {
            name = (String) m.get("name");
        }
        if(name == null) {
            throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "未找到uri");
        }
        query = "MATCH (n:`Resource`) WHERE n.`uri` = $uri ";
        if(prev != null && prev.getSubject() != null) {
//            String proCode = RuleHandler.getPropertyAbbrByName(subject, prev.getSubject());
//            if(proCode == null) {
//                throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "找不到" + subject + "的属性" + prev.getSubject());
//            }
            query += " AND n.`" + prev.getPredicate() + "` = $oldValue REMOVE n.`" + prev.getPredicate() + "` ";
            map.put("oldValue", prev.getObject());
        }
        if(next != null && next.getSubject() != null) {
//            String proCode = RuleHandler.getPropertyAbbrByName(subject, next.getSubject());
//            if(proCode == null) {
//                throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "找不到" + subject + "的属性" + next.getSubject());
//            }
            query += " SET n.`" + next.getPredicate() + "` = $newValue ";
            map.put("newValue", next.getObject());
        }
        query += "RETURN ID(n) as id";
        result = session.query(query, map);
        for (Map<String, Object> m : result.queryResults()) {
            if(m.size() == 0) {
                throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "实体属性不匹配");
            }
        }
    }

    /**
     * 增加或删除关系
     * 注：pre和next的predicate要求是确定存在的，其合理性判断应由上层逻辑负责
     * @param pre pre存在，删除关系
     * @param next next存在，新增关系
     */
    public void updateRelation(Relation pre, Relation next) {
        if(next != null && !StringUtils.isEmpty(next.getPredicate())) {
//            String subject = RuleHandler.getSubjectByUri(next.getSubjectUri());
//            String newType = RuleHandler.getPropertyAbbrByName(subject, next.getPredicate());
//            if(StringUtils.isEmpty(newType)) {
//                throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "找不到关系名" + next.getPredicate());
//                newType = next.getPredicate();
//            }
            String query = "MATCH (n:`Resource` { uri: $fromUri }), (m:`Resource` { uri: $toUri }) " +
                    "CREATE (n)-[e:`" + next.getPredicate() + "`]->(m) " +
                    "RETURN ID(e) as id";
            Map<String, Object> map = new HashMap<>();
            map.put("fromUri", next.getSubjectUri());
            map.put("toUri", next.getObjectUri());
            Result result = session.query(query, map);
            for (Map<String, Object> m : result.queryResults()) {
                if(m.size() == 0) {
                    throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "无法创建新关系");
                }
            }
        }
        if(pre != null && !StringUtils.isEmpty(pre.getPredicate())) {
//            String subject = RuleHandler.getSubjectByUri(pre.getSubjectUri());
//            String oldType = RuleHandler.getPropertyAbbrByName(subject, pre.getPredicate());
//            if(StringUtils.isEmpty(oldType)) {
//                throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "找不到关系名" + pre.getPredicate());
//                oldType = pre.getPredicate();
//            }
            String query = "MATCH (n:`Resource` { uri: $fromUri })-[e: `" + pre.getPredicate() + "`]->(m:`Resource` { uri: $toUri })" +
                    "DELETE e RETURN ID(e) as id";
            Map<String, Object> map = new HashMap<>();
            map.put("fromUri", pre.getSubjectUri());
            map.put("toUri", pre.getObjectUri());
            Result result = session.query(query, map);
            for (Map<String, Object> m : result.queryResults()) {
                if(m.size() == 0) {
                    throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "无法删除关系");
                }
            }
        }
    }

    public void updateLabels(String uri, List<String> labels) {
        String query = "MATCH (n:`Resource` { uri : $uri }) RETURN n.`rdfs__label` as name, labels(n) as labels";
        Map<String, Object> map = new HashMap<>();
        map.put("uri", uri);
        Result result = session.query(query, map);
        String name = "";
        List<String> oldLabels = new ArrayList<>();
        for (Map<String, Object> m : result.queryResults()) {
            if(m.size() == 0) {
                throw new BusinessException(WebConstant.CUSTOMIZE_ERROR, "找不到uri");
            }
            name = (String) m.get("name");
            oldLabels = Arrays.asList((String[]) m.get("labels"));
        }
        if(oldLabels.size() != 0) {
            query = "MATCH (n:`Resource` { uri: $uri }) REMOVE n:`" + String.join("`:`", oldLabels) + "`";
            session.query(query, map);
        }
        query = "MATCH (n { uri: $uri }) SET n:`" + String.join("`:`", labels) + "`";
        session.query(query, map);
    }

    public synchronized List<QuikAddEntitiesVO> batchAddEntities(String subject, String label, List<Entity> entities) {
        List<QuikAddEntitiesVO> quikAddEntitiesVOList = new ArrayList<>();
        // 从缓存中获取学科的最大id（该方法包含了缓存如果不存在就去数据库查询的逻辑）
        Integer maxId = redisManager.getMaxIdWithSubject(subject);
        List<String> uriList = RuleHandler.generateSubjectUris(maxId, subject, entities.size());
        String subjectLabel = RuleHandler.convertSubject2Label(subject);
        List<String> labels = Arrays.asList("Resource", label, subjectLabel);
        String labelStr = "`" + String.join("`:`", labels) + "`";
        List<String> nodes = new ArrayList<>();
        for(int i=0; i<entities.size(); i++) {
            Entity entity = entities.get(i);
            String name = entity.getName();
            String uri = uriList.get(i);
            List<String> params = new ArrayList<>();
            params.add(String.format("`%s`:'%s'", "uri", uri));
            params.add(String.format("`%s`:'%s'", "rdfs__label", name));
            for(Property property : entity.getProperty()) {
                String propertyAbbr = property.getPredicate();
                if(StringUtils.isEmpty(propertyAbbr)) {
                    continue;
                }
                params.add(String.format("`%s`:'%s'", propertyAbbr, property.getObject()));
            }
            String propertyStr = String.join(",", params);
            nodes.add(String.format("(n%d:%s {%s})", i, labelStr, propertyStr));
            quikAddEntitiesVOList.add(new QuikAddEntitiesVO(name, uri));
        }
        String query = "CREATE " + String.join(",", nodes);
        session.query(query, new HashMap<>());
        // 插入数据库成功后，执行，更新目前的最大id
        redisManager.subjectIdIncr(subject, entities.size());
        return quikAddEntitiesVOList;
    }

    public void deleteEntityByUri(String uri) {
        String query = "MATCH (n:`Resource` { uri: '" + uri + "'}) DELETE n";
        Entity entity = getEntityFromUri(uri);
        if(entity.getUri() == null) {
            return;
        }
        for(Relation relation : entity.getRelation()) {
            updateRelation(relation, null);
        }
        session.query(query, new HashMap<>());
    }

    int scoreCalculator(String source, String target, boolean fromName) {
        int appearPosPunish = 10;
        int propertyPunish = 10;
        int pos = source.equals(target) ? 0 : source.split(target)[0].length();
        int score = source.length() + pos * appearPosPunish;
        return fromName ? score : score * propertyPunish;
    }
}
