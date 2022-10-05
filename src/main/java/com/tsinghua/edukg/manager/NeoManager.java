package com.tsinghua.edukg.manager;

import com.tsinghua.edukg.model.Entity;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NeoManager {

    @Autowired
    @Qualifier("neo4jSession")
    Session session;

    public List<String> getUrisFromKeyword(String keyword, String subject, String label, Integer page, Integer size) {
        String query = "MATCH (n:Resource{%s}) %s RETURN n.uri skip $skip LIMIT $limit";
        Map<String, String> map = new HashMap<>();
        map.put("skip", String.valueOf(page * size));
        map.put("limit", String.valueOf(size));
        List<String> conditions = new ArrayList<>();
        if(!StringUtils.isEmpty(subject)) {
            map.put("subject", subject);
            conditions.add("subject:$subject");
        }
        if(!StringUtils.isEmpty(label)) {
            label = label.replace("'", "");
            map.put("label", label);
            conditions.add("label:$label");
        }
        String param2 = "";
        if(!StringUtils.isEmpty(keyword)) {
            keyword = keyword.replace(".", "").replace("*", "").replace("\"", "");
            map.put("keyword", keyword);
            param2 = "WHERE n.uri=~ \".*$keyword.*\"";
        }
        String param1 = "";
        if(conditions.size() != 0) {
            param1 = String.join(",", conditions);
        }
        query = String.format(query, param1, param2);
        List<String> retList = new ArrayList<>();
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
            Entity entity = new Entity((String) m.get("rdfs__label"), (String) m.get("uri"), (List<String>) m.get("labels"), (Map<String, String>) m.get("properties"));
            entities.add(entity);
        }
        return entities;
    }

    public List<Entity> getEntityListFromUrisTest(List<String> heights) {
        String query = "MATCH (n:Person) WHERE n.height IN $heights RETURN properties(n) as properties";
        Map<String, Object> map = new HashMap<>();
        map.put("heights", heights);
        Result result = session.query(query, map);
        List<Entity> entities = new ArrayList<>();
        for (Map<String, Object> m : result.queryResults()) {
            Entity entity = new Entity();
            Map<String, String> propertyMap = (Map<String, String>) m.get("properties");
            entity.setPropertyMap(propertyMap);
            entities.add(entity);
        }
        return entities;
    }

    public List<Entity> getEntityListFromUrisTest2(List<String> heights) {
        String query = "MATCH (n:Person) WHERE n.height IN $heights RETURN n";
        Map<String, Object> map = new HashMap<>();
        map.put("heights", heights);
        Result result = session.query(query, map);
        List<Entity> entities = new ArrayList<>();
        for (Map<String, Object> m : result.queryResults()) {
            for(Map.Entry<String, Object> entry : m.entrySet()) {
                String name = (String) entry.getValue();
                System.out.println(name);
            }
        }
        return entities;
    }
}
