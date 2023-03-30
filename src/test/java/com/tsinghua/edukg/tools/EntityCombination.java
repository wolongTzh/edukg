package com.tsinghua.edukg.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.RuleHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class EntityCombination {

    @Resource
    NeoManager neoManager;

    @Autowired
    @Qualifier("neo4jSession")
    Session session;

    /**
     * 1 : 同名实体合并
     * 2 : 别名实体合并
     */
    Integer entityType = 1;

    /**
     * 统计同名实体和同名的个数
     * commonNameName（写）：同名的实体，个数
     * @throws IOException
     */
    @Test
    public void getSameNameEntity() throws IOException {
        List<String> nameList = new ArrayList<>();
        Set<String> nameRecord = new HashSet<>();
        String query = "match (n:Resource)" +
                " return n.rdfs__label as name";
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            nameList.add((String) m.get("name"));
        }
        File file =new File("./commonNameName.txt");
        if(!file.exists()){
            file.createNewFile();
        }
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        StringBuilder sb = new StringBuilder();
        int progressCount = 0;
        for(String name : nameList) {
            progressCount++;
            query = "match (n:Resource) where n.rdfs__label=\"" + name + "\"" +
                    " return n.rdfs__label as name";
            try {
                result = session.query(query, new HashMap<>());
            }
            catch (Exception e) {
                continue;
            }
            int count = 0;
            String line = "";
            for (Map<String, Object> m : result.queryResults()) {
                count++;
                if(count == 2 && !nameRecord.contains((String) m.get("name"))) {
                    line += (String) m.get("name") + " ";
                    nameRecord.add((String) m.get("name"));
                }
            }
            if(count >= 2 && !line.equals("")) {
                line += count;
            }
            if(!line.equals("")) {
                sb.append(line + "\n");
            }
            if(progressCount % 1000 == 0) {
                System.out.println("progress = " + String.format("%.2f", progressCount * 1.0 / nameList.size()));
                fileWritter.write(sb.toString());
                fileWritter.flush();
                sb.delete(0, sb.length());
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    /**
     * 将实体的详细信息以json格式输出
     * commonNameName（读）
     * entityJsonOut（写）
     * @throws IOException
     */
    @Test
    public void produceSameNameInfo() throws IOException {
        String query;
        File file =new File("./commonNameName.txt");
        List<String> contents = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        int progressCount = 0;
        Result result;
        File fileOut =new File("./entityJsonOut.txt");
        if(!fileOut.exists()){
            fileOut.createNewFile();
        }
        FileWriter fileWritter = new FileWriter(fileOut.getName(),true);
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        for(String content : contents) {
            Matcher matcher = pattern.matcher(content);
            if(!matcher.find()) {
                continue;
            }
            String name = content.split(" ")[0];
            progressCount++;
            query = "match (n:Resource) where n.rdfs__label=\"" + name + "\"" +
                    " return n.rdfs__label as name, labels(n) as labels, n.uri as uri";
            try {
                result = session.query(query, new HashMap<>());
            }
            catch (Exception e) {
                continue;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            List<String> classList = new ArrayList<>();
            List<String> uriList = new ArrayList<>();
            for (Map<String, Object> m : result.queryResults()) {
                classList.addAll(Arrays.asList((String[]) m.get("labels")));
                uriList.add((String) m.get("uri"));
            }
            jsonObject.put("classList", classList);
            jsonObject.put("uriList", uriList);
            sb.append(JSON.toJSONString(jsonObject) + "\n");
            if(progressCount % 100 == 0) {
                System.out.println("progress = " + String.format("%.2f", progressCount * 1.0 / contents.size()));
                fileWritter.write(sb.toString());
                fileWritter.flush();
                sb.delete(0, sb.length());
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    /**
     * 提取模板中的信息并合并到目标实体
     * xx的xx由于存在有用信息（定义、性质）所以需要将其合并到xx中
     * deleteUriRecord（写）：被合并完的实体要删掉
     *
     * @throws IOException
     */
    @Test
    public void combineTemplateEntity() throws IOException {
        String query = "match (n:Resource)" +
                " return n.rdfs__label as name";
        Result result = session.query(query, new HashMap<>());
        Set<String> nameSet = new HashSet<>();
        File deleteUriRecord = new File("./deleteUriRecord.txt");
        FileWriter fileWritter = new FileWriter(deleteUriRecord.getName(),true);
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> m : result.queryResults()) {
            try {
                String name = (String) m.get("name");
                nameSet.add(name);
            }
            catch (Exception e) {
                continue;
            }
        }
        for (Map<String, Object> m : result.queryResults()) {
            try {
                String name = (String) m.get("name");
                if (name.split("的").length == 2) {
                    String prefix = name.split("的")[0];
                    String suffix = name.split("的")[1];
                    if (nameSet.contains(prefix) && !StringUtils.isEmpty(RuleHandler.getPropertyAbbrWithoutSubject(suffix))) {
                        List<Entity> entityList = neoManager.getEntityListFromName(name);
                        for(Entity entity : entityList) {
                            Entity concrateEntity = neoManager.getEntityFromUri(entity.getUri());
                            List<Property> propertyList = concrateEntity.getProperty();
                            RuleHandler.propertyConverter(propertyList);
                            for(Property property : propertyList) {
                                if(property.getPredicateLabel().equals("内容") || property.getPredicateLabel().equals("包含")) {
                                    List<Entity> targetEntityList = neoManager.getEntityListFromName(prefix);
                                    Entity targetEntity = neoManager.getEntityFromUri(targetEntityList.get(0).getUri());
                                    String temps = RuleHandler.getPropertyAbbrWithoutSubject(suffix);
                                    Property propertyAdd = Property.builder()
                                            .subject(prefix)
                                            .object(property.getObject())
                                            .predicate(property.getPredicate())
                                            .build();
                                    neoManager.updateProperty(targetEntity.getUri(), propertyAdd, null);
                                    sb.append(entity.getUri() + " " + entity.getName() + "\n");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                continue;
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    /**
     * 实体合并工具（包括同名和别名合并，由全局变量entityType控制）
     * 将两实体关系和属性都合并到其中一个实体中，将另一实体删除
     * 如果是别名实体合并，还需要在被合并的实体中添加别名属性
     * name2uri（读）：秋阳生成的json文件，里面说明了要将哪些实体合并到哪个实体上，其中result为主要实体
     * failedRecord（写）：记录合并失败的实体名称与uri
     * deleteUriRecord（需要删除的实体记录）
     * @throws IOException
     */
    @Test
    public void combineEntity() throws IOException {
        String query;
        Result result;
        JSONArray nameUriArray = CommonUtil.readJsonOut("./name2uri.json").getJSONArray("content");
        File failedRecord = new File("./failedRecord.txt");
        File deleteUriRecord = new File("./deleteUriRecord.txt");
        FileWriter fileWritter = new FileWriter(failedRecord.getName(),true);
        FileWriter deleteUriRecordWritter = new FileWriter(deleteUriRecord.getName(),true);
        StringBuilder sb = new StringBuilder();
        StringBuilder deleteUriRecordSb = new StringBuilder();
        for(int i=0; i<nameUriArray.size(); i++) {
            JSONObject nameUri = nameUriArray.getJSONObject(i);
            String name = (String) nameUri.get("name");
            List<String> uris = (List<String>) nameUri.get("list");
            String remainUri = (String) nameUri.get("result");
            try {
                List<Property> propertyList = new ArrayList<>();
                List<Relation> relationList = new ArrayList<>();
                List<Property> mainPropertyList = new ArrayList<>();
                List<Relation> mainRelationList = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                query = "match (n:Resource) where n.rdfs__label=\"" + name + "\"" +
                        " return n.rdfs__label as name, n.uri as uri, labels(n) as labels";
                try {
                    result = session.query(query, new HashMap<>());
                }
                catch (Exception e) {
                    continue;
                }
                List<String> aliasList = new ArrayList<>();
                for (Map<String, Object> m : result.queryResults()) {
                    String uri = (String) m.get("uri");
                    Entity entity = neoManager.getEntityFromUri(uri);
                    if(uri.equals(remainUri)) {
                        mainPropertyList.addAll(entity.getProperty());
                        mainRelationList.addAll(entity.getRelation());
                    }
                    else {
                        aliasList.add(entity.getName());
                        deleteUriRecordSb.append(uri + " " + entity.getName() + "\n");
                        propertyList.addAll(entity.getProperty());
                        relationList.addAll(entity.getRelation());
                    }
                    List<String> newLabels = Arrays.asList((String[]) m.get("labels"));
                    for(String label : newLabels) {
                        if(!labels.contains(label)) {
                            labels.add(label);
                        }
                    }
                }
                Map<String, String> reflectionMap = new HashMap<>();
                for(String uri : uris) {
                    reflectionMap.put(uri, remainUri);
                }
                updateRelation(reflectionMap, relationList);
                List<Property> needAddProperty = combineProperty(mainPropertyList, propertyList);
                if(entityType == 2) {
                    for(String alias : aliasList) {
                        needAddProperty.add(Property.builder()
                                .subject(name)
                                .object(alias)
                                .predicate("edukg_prop_common__main-P1")
                                .build());
                    }
                }
                List<Relation> needAddRelation = combineRelation(mainRelationList, relationList);
                for(Property property : needAddProperty) {
                    neoManager.updateProperty(remainUri, null, property);
                }
                for(Relation relation : needAddRelation) {
                    neoManager.updateRelation(null, relation);
                }
                neoManager.updateLabels(remainUri, labels);
            }
            catch (Exception e) {
                sb.append(name + " " + remainUri + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
        deleteUriRecordWritter.write(deleteUriRecordSb.toString());
        deleteUriRecordWritter.flush();
        deleteUriRecordWritter.close();
    }

    List<Property> combineProperty(List<Property> mainProperties, List<Property> propertyList) {
        List<Property> needAddProperty = new ArrayList<>();
        for(Property property : propertyList) {
            String predicate = property.getPredicate();
            String object = property.getObject();
            boolean needAdd = true;
            for(Property mainProperty : mainProperties) {
                String mainPredicate = mainProperty.getPredicate();
                String mainObject = mainProperty.getObject();
                if(predicate.contains("xlore") && !predicate.equals("edukg_prop_politics__xlore-P0")) {
                    if(object.equals(mainObject)) {
                        needAdd = false;
                    }
                }
                else {
                    if(predicate.equals(mainPredicate)) {
                        needAdd = false;
                    }
                }
            }
            if(needAdd) {
                needAddProperty.add(property);
            }
        }
        return needAddProperty;
    }

    List<Relation> combineRelation(List<Relation> mainRelations, List<Relation> relationList) {
        List<Relation> needAddRelation = new ArrayList<>();
        for(Relation relation : relationList) {
            String predicate = relation.getPredicate();
            String subjectUri = relation.getSubjectUri();
            String objectUri = relation.getObjectUri();
            boolean needAdd = true;
            for(Relation mainRelation : mainRelations) {
                String mainPredicate = mainRelation.getPredicate();
                String mainSubjectUri = mainRelation.getSubjectUri();
                String mainObjectUri = mainRelation.getObjectUri();
                needAdd = !(Objects.equals(predicate, mainPredicate) && Objects.equals(subjectUri, mainSubjectUri) && Objects.equals(objectUri, mainObjectUri));
                if(!needAdd) {
                    break;
                }
            }
            if(needAdd) {
                needAddRelation.add(relation);
            }
        }
        return needAddRelation;
    }

    void updateRelation(Map<String, String> reflectionMap, List<Relation> relationList) {
        for(Relation relation : relationList) {
            String subjectUri = reflectionMap.get(relation.getSubjectUri()) == null ? relation.getSubjectUri() : reflectionMap.get(relation.getSubjectUri());
            String objectUri = reflectionMap.get(relation.getObjectUri()) == null ? relation.getObjectUri() : reflectionMap.get(relation.getObjectUri());
            relation.setSubjectUri(subjectUri);
            relation.setObjectUri(objectUri);
        }
    }

    /**
     * 实体关系uri更新工具（已废弃）
     * 更新关系中的uri，更新为所保留的uri
     * uriReflection格式（读）: 被合并的实体uri，要合并进去的实体uri
     * 已废弃，该工具原本用于实体合并的第二步，将uri更新，但后来发现可以一步到位
     */
    @Test
    public void updateRelationIndep() {
        Map<String, String> reflectionMap = readUriReflection();
        File file =new File("./uriReflection.txt");
        List<String> contents = new ArrayList<>();
        Set<String> uriSet = new HashSet<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String content : contents) {
            String[] uris = content.split(" ");
            if(uris.length < 2) {
                continue;
            }
            uriSet.addAll(Arrays.asList(uris).subList(1, uris.length));
        }
        for(String uri : uriSet) {
            Entity entity = neoManager.getEntityFromUri(uri);
            List<Relation> relationList = entity.getRelation();
            for(Relation relation : relationList) {
                String subjectUri = reflectionMap.get(relation.getSubjectUri()) == null ? relation.getSubjectUri() : reflectionMap.get(relation.getSubjectUri());
                String objectUri = reflectionMap.get(relation.getObjectUri()) == null ? relation.getObjectUri() : reflectionMap.get(relation.getObjectUri());
                Relation newRelation = Relation.builder()
                        .predicate(relation.getPredicate())
                        .subject(relation.getSubject())
                        .subjectUri(subjectUri)
                        .object(relation.getObject())
                        .objectUri(objectUri)
                        .build();
                neoManager.updateRelation(relation, newRelation);
            }
        }
    }

    Map<String, String> readUriReflection() {
        Map<String, String> reflectionMap = new HashMap<>();
        File file =new File("./uriReflection.txt");
        List<String> contents = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String content : contents) {
            String[] uriList = content.split(" ");
            if(uriList.length < 2) {
                continue;
            }
            String mainUri = uriList[0];
            for(int i=1; i<uriList.length; i++) {
                String uri = uriList[i];
                reflectionMap.put(uri, mainUri);
            }
        }
        return reflectionMap;
    }

}
