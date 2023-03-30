package com.tsinghua.edukg.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class GraphEditHelper {

    @Autowired
    NeoManager neoManager;

    String failedPath = "./failedRecord.txt";

    /**
     * 添加关系
     * @throws IOException
     */
    @Test
    public void addRelation() throws IOException {
        String path = "./needAdderJson.txt";
        List<String> contents = CommonUtil.readTextFromPath(path);
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        StringBuilder sb = new StringBuilder();
        for(String content : contents) {
            try {
                Relation relation = JSON.parseObject(content, Relation.class);
                String subjectUri = relation.getSubjectUri();
                Entity entity = neoManager.getEntityFromUri(subjectUri);
                if(entity.getUri() == null) {
                    sb.append(content + "\n");
                    continue;
                }
                List<Relation> objectRelations = entity.getRelation();
                boolean needAddTag = true;
                for(Relation r : objectRelations) {
                    if(r.getSubject().equals(relation.getSubject()) &&
                            r.getObject().equals(relation.getObject()) &&
                            r.getPredicate().equals(relation.getPredicate())) {
                        needAddTag = false;
                    }
                }
                if(needAddTag) {
                    log.info(JSON.toJSONString(relation));
                    neoManager.updateRelation(null, relation);
                }
            }
            catch (Exception e) {
                sb.append(content + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    /**
     * 添加关系
     * @throws IOException
     */
    @Test
    public void addRelationWithPureJSON() throws IOException {
        String path = "./relation_triples.json";
        JSONArray outerArray = CommonUtil.readJsonArray(path);
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<outerArray.size(); i++) {
            try {
                Relation relation = outerArray.getObject(i, Relation.class);
                String subjectUri = relation.getSubjectUri();
                Entity subjectEntity;
                if(StringUtils.isEmpty(subjectUri)) {
                    List<Entity> entityList = neoManager.getEntityListFromName(relation.getSubject());
                    subjectEntity = neoManager.getEntityFromUri(entityList.get(0).getUri());
                    relation.setSubjectUri(subjectEntity.getUri());
                }
                else {
                    subjectEntity = neoManager.getEntityFromUri(subjectUri);
                }
                if(subjectEntity.getUri() == null) {
                    sb.append(outerArray.getJSONObject(i).toJSONString() + "\n");
                    continue;
                }
                Entity objectEntity;
                if(StringUtils.isEmpty(relation.getObjectUri())) {
                    List<Entity> entityList = neoManager.getEntityListFromName(relation.getSubject());
                    objectEntity = entityList.get(0);
                    relation.setObjectUri(objectEntity.getUri());
                }
                else {
                    objectEntity = neoManager.getEntityFromUri(relation.getObjectUri());
                }
                if(objectEntity.getUri() == null) {
                    sb.append(outerArray.getJSONObject(i).toJSONString() + "\n");
                    continue;
                }
                List<Relation> subjectRelations = subjectEntity.getRelation();
                boolean needAddTag = true;
                if(!CollectionUtils.isEmpty(subjectRelations)) {
                    for(Relation r : subjectRelations) {
                        if(r.getSubject().equals(relation.getSubject()) &&
                                r.getObject().equals(relation.getObject()) &&
                                r.getPredicate().equals(relation.getPredicate())) {
                            needAddTag = false;
                        }
                    }
                }
                if(needAddTag) {
                    log.info(JSON.toJSONString(relation));
//                    neoManager.updateRelation(null, relation);
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

    /**
     * 添加属性
     */
    @Test
    public void addProperty() throws IOException {
        String path = "./uriReflection.txt";
        List<String> contents = CommonUtil.readTextFromPath(path);
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        StringBuilder sb = new StringBuilder();
        for(String content : contents) {
            try{
                JSONObject jsonObject = JSON.parseObject(content);
                String uri = jsonObject.getString("uri");
                Property property = jsonObject.getObject("property", Property.class);
                Entity entity = neoManager.getEntityFromUri(uri);
                if(StringUtils.isEmpty(entity.getUri())) {
                    sb.append(content + "\n");
                    continue;
                }
                boolean needAddTag = true;
                for(Property p : entity.getProperty()) {
                    if(p.getSubject().equals(property.getSubject()) &&
                            p.getObject().equals(property.getObject()) &&
                            p.getPredicate().equals(property.getPredicate())) {
                        needAddTag = false;
                    }
                }
                if(needAddTag) {
                    log.info(JSON.toJSONString(property));
//                neoManager.updateProperty(uri, null, property);

                }
            }
            catch (Exception e) {
                sb.append(content + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    /**
     * 添加属性
     */
    @Test
    public void addPropertyWithPureJSON() throws IOException {
        String path = "./prop_triples.json";
        JSONArray outerArray  = CommonUtil.readJsonArray(path);
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),false);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<outerArray.size(); i++) {
            JSONObject innerObject = outerArray.getJSONObject(i);
            try {
                String uri = innerObject.getString("uri");
                Property property = innerObject.getObject("property", Property.class);
                Entity entity;
                if(StringUtils.isEmpty(uri)) {
                    List<Entity> entityList = neoManager.getEntityListFromName(property.getSubject());
                    entity = neoManager.getEntityFromUri(entityList.get(0).getUri());
                    uri = entity.getUri();
                }
                else {
                    entity = neoManager.getEntityFromUri(uri);
                }
                if(StringUtils.isEmpty(entity.getUri())) {
                    sb.append(innerObject.toJSONString() + "\n");
                    continue;
                }
                boolean needAddTag = true;
                if(!CollectionUtils.isEmpty(entity.getProperty())) {
                    for(Property p : entity.getProperty()) {
                        if(p.getSubject().equals(property.getSubject()) &&
                                p.getObject().equals(property.getObject()) &&
                                p.getPredicate().equals(property.getPredicate())) {
                            needAddTag = false;
                        }
                    }
                }
                if(needAddTag) {
                    log.info(JSON.toJSONString(property));
//                    neoManager.updateProperty(uri, null, property);
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

    /**
     * 添加实体
     * @throws IOException
     */
    @Test
    public void addEntity() throws IOException {
        String path = "./";
        List<String> contents = CommonUtil.readTextFromPath(path);
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        StringBuilder sb = new StringBuilder();
        for(String content : contents) {
            try {
                JSONObject jsonObject = JSON.parseObject(content);
                String name = jsonObject.getString("name");
                String subject = jsonObject.getString("subject");
                if(StringUtils.isEmpty(name) || StringUtils.isEmpty(subject)) {
                    sb.append(content + "\n");
                    continue;
                }
                Entity entity = jsonObject.getObject("entity", Entity.class);
                List<Entity> entityJudge = neoManager.getEntityListFromName(name);
                if(entityJudge.size() != 0) {
                    sb.append(content + "\n");
                    continue;
                }
                List<Entity> entityList = new ArrayList<>();
                entityList.add(entity);
                log.info(JSON.toJSONString(entity));
//            neoManager.batchAddEntities(name, subject, entityList);
            }
            catch (Exception e) {
                sb.append(content + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    /**
     * 添加实体
     * @throws IOException
     */
    @Test
    public void addEntityWithPureJSON() throws IOException {
        String path = "./new_entities0328.json";
        JSONArray outerArray  = CommonUtil.readJsonArray(path);
        File file = new File(failedPath);
        FileWriter fileWritter = new FileWriter(file.getName(),false);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<outerArray.size(); i++) {
            JSONObject innerObject = outerArray.getJSONObject(i);
            try {
                String label = innerObject.getString("label");
                String subject = innerObject.getString("subject");
                if(StringUtils.isEmpty(label) || StringUtils.isEmpty(subject)) {
                    continue;
                }
                JSONArray innerArray = innerObject.getJSONArray("entities");
                List<Entity> entityList = new ArrayList<>();
                for(int j=0; j<innerArray.size(); j++) {
                    Entity entity = innerArray.getObject(j, Entity.class);
                    List<Entity> entityJudge = neoManager.getEntityListFromName(entity.getName());
                    if(entityJudge.size() != 0) {
                        sb.append(innerObject.toJSONString() + "\n");
                        continue;
                    }
                    Property property = Property.builder()
                            .subject(entity.getName())
                            .object(entity.getName())
                            .predicate("rdfs__label")
                            .build();
                    List<Property> properties;
                    if(CollectionUtils.isEmpty(entity.getProperty())) {
                        properties = new ArrayList<>();
                    }
                    else {
                        properties = entity.getProperty();
                    }
                    properties.add(property);
                    entity.setProperty(properties);
                    entityList.add(entity);
                    log.info(JSON.toJSONString(entity));
                }
//                neoManager.batchAddEntities(subject, label, entityList);
            }
            catch (Exception e) {
                sb.append(innerObject.toJSONString() + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    /**
     * 删除实体
     * 根据uri删除实体
     * deleteUriRecord（读）
     * @throws IOException
     */
    @Test
    public void deleteByUri() throws IOException {
        File file =new File("./deleteUriRecord.txt");
        File failedRecord = new File(failedPath);
        FileWriter fileWritter = new FileWriter(failedRecord.getName(),true);
        StringBuilder sb = new StringBuilder();
        List<String> contents = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String content : contents) {
            String uri = content.split(" ")[1];
            try{
                neoManager.deleteEntityByUri(uri);
            }
            catch (Exception e) {
                e.printStackTrace();
                sb.append(content + "\n" + e.getMessage() + "\n\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }
}