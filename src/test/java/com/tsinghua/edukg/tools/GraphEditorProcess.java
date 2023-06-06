package com.tsinghua.edukg.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.VO.QuikAddEntitiesVO;
import com.tsinghua.edukg.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class GraphEditorProcess {

    @Autowired
    NeoManager neoManager;

    @Test
    public void mainProcess() throws IOException {

        String entityAddPath = "./newEntity.json";
        String entityAddFailedPath = "./new_entity_failed.txt";
        String propAddPath = "./addPropEntity.json";
        String propAddFailedPath = "./prop_add_failed.txt";
        String relationAddPath = "./addRelation.json";
        String relationAddFailedPath = "./relation_add_failed.txt";

        Map<String, String> nameMap = addEntityWithPureJSON(entityAddPath, entityAddFailedPath);
        JSONArray propJsonArray = CommonUtil.readJsonArray(propAddPath);
        for(int i=0; i<propJsonArray.size(); i++) {
            JSONObject jsonObject = propJsonArray.getJSONObject(i);
            String uri = jsonObject.getString("uri");
            Property property = jsonObject.getObject("property", Property.class);
            if(uri.equals("")) {
                String realUri = nameMap.get(property.getSubject());
                jsonObject.put("uri", realUri);
            }
        }
        JSONArray relationJsonArray = CommonUtil.readJsonArray(relationAddPath);
        for(int i=0; i<relationJsonArray.size(); i++) {
            JSONObject jsonObject = relationJsonArray.getJSONObject(i);
            String uri = jsonObject.getString("subjectUri");
            String subject = jsonObject.getString("subject");
            if(uri.equals("")) {
                String realUri = nameMap.get(subject);
                jsonObject.put("subjectUri", realUri);
            }
            uri = jsonObject.getString("objectUri");
            String object = jsonObject.getString("object");
            if(uri.equals("")) {
                String realUri = nameMap.get(object);
                jsonObject.put("objectUri", realUri);
            }
        }
        addPropertyWithPureJSON(propJsonArray, propAddFailedPath);
        addRelationWithPureJSON(relationJsonArray, relationAddFailedPath);
    }

    public Map<String, String> addEntityWithPureJSON(String path, String failedPath) throws IOException {
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),false);
        List<QuikAddEntitiesVO> quikAddEntitiesVOList = new ArrayList<>();
        Map<String, String> nameMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        JSONArray outerArray  = CommonUtil.readJsonArray(path);
        for(int i=0; i<outerArray.size(); i++) {
            JSONObject jsonObject = outerArray.getJSONObject(i);
            try {
                String label = jsonObject.getString("label");
                String subject = jsonObject.getString("subject");
                if (StringUtils.isEmpty(label) || StringUtils.isEmpty(subject)) {
                    return nameMap;
                }
                JSONArray innerArray = jsonObject.getJSONArray("entities");
                List<Entity> entityList = new ArrayList<>();
                for (int j = 0; j < innerArray.size(); j++) {
                    Entity entity = innerArray.getObject(j, Entity.class);
//                    List<Entity> entityJudge = neoManager.getEntityListFromName(entity.getName());
//                    if (entityJudge.size() != 0) {
//                        sb.append(jsonObject.toJSONString() + "\n");
//                        continue;
//                    }
                    Property property = Property.builder()
                            .subject(entity.getName())
                            .object(entity.getName())
                            .predicate("rdfs__label")
                            .build();
                    List<Property> properties;
                    if (CollectionUtils.isEmpty(entity.getProperty())) {
                        properties = new ArrayList<>();
                    } else {
                        properties = entity.getProperty();
                    }
                    properties.add(property);
                    entity.setProperty(properties);
                    entityList.add(entity);
                    log.info(JSON.toJSONString(entity));
                }
                quikAddEntitiesVOList.addAll(neoManager.batchAddEntities(subject, label, entityList));
            }
            catch (Exception e) {
                sb.append(jsonObject.toJSONString() + "\n");
            }
        }

        for(QuikAddEntitiesVO quikAddEntitiesVO : quikAddEntitiesVOList) {
            nameMap.put(quikAddEntitiesVO.getName(), quikAddEntitiesVO.getUri());
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
        return nameMap;
    }

    public void addPropertyWithPureJSON(JSONArray outerArray, String failedPath) throws IOException {
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),false);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<outerArray.size(); i++) {
            JSONObject innerObject = outerArray.getJSONObject(i);
            try {
                String uri = innerObject.getString("uri");
                Property property = innerObject.getObject("property", Property.class);
                Entity entity;
                entity = neoManager.getEntityFromUri(uri);
                if(StringUtils.isEmpty(entity.getUri())) {
                    sb.append(innerObject.toJSONString() + "\n");
                    continue;
                }
                boolean needAddTag = true;
//                if(!CollectionUtils.isEmpty(entity.getProperty())) {
//                    for(Property p : entity.getProperty()) {
//                        if(p.getSubject().equals(property.getSubject()) &&
//                                p.getObject().equals(property.getObject()) &&
//                                p.getPredicate().equals(property.getPredicate())) {
//                            needAddTag = false;
//                        }
//                    }
//                }
                if(needAddTag) {
                    log.info(JSON.toJSONString(property));
                    neoManager.updateProperty(uri, null, property);
                }
            }
            catch (Exception e) {
                sb.append(innerObject.toJSONString() + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    public void addRelationWithPureJSON(JSONArray outerArray, String failedPath) throws IOException {
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<outerArray.size(); i++) {
            try {
                Relation relation = outerArray.getObject(i, Relation.class);
                String subjectUri = relation.getSubjectUri();
                Entity subjectEntity;
                subjectEntity = neoManager.getEntityFromUri(subjectUri);
                if(subjectEntity.getUri() == null) {
                    sb.append(outerArray.getJSONObject(i).toJSONString() + "\n");
                    continue;
                }
                Entity objectEntity;
                objectEntity = neoManager.getEntityFromUri(relation.getObjectUri());

                if(objectEntity.getUri() == null) {
                    sb.append(outerArray.getJSONObject(i).toJSONString() + "\n");
                    continue;
                }
                List<Relation> subjectRelations = subjectEntity.getRelation();
                boolean needAddTag = true;
//                if(!CollectionUtils.isEmpty(subjectRelations)) {
//                    for(Relation r : subjectRelations) {
//                        if(r.getSubject().equals(relation.getSubject()) &&
//                                r.getObject().equals(relation.getObject()) &&
//                                r.getPredicate().equals(relation.getPredicate())) {
//                            needAddTag = false;
//                        }
//                    }
//                }
                if(needAddTag) {
                    log.info(JSON.toJSONString(relation));
                    neoManager.updateRelation(null, relation);
                }
            }
            catch (Exception e) {
                sb.append(outerArray.getJSONObject(i).toJSONString() + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }
}
