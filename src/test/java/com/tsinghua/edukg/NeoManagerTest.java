package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.EntityWithScore;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
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
public class NeoManagerTest {

    @Resource
    NeoManager neoManager;

    @Autowired
    @Qualifier("neo4jSession")
    Session session;

    @Test
    public void getBareEntityFromNameTest() {
        String name = "基因";
        List<Entity> entityList = neoManager.getEntityListFromName(name);
        log.info(JSON.toJSONString(entityList));
    }

    @Test
    public void getUrisFromKeywordWithPageTest() {
        String keyword = "main";
        String subject = "化学";
        String label = "edukg_cls_chemistry__main-C0";
        Integer page = 0;
        Integer size = 50;
        List<String> resultList = neoManager.getUrisFromKeywordWithPage(keyword, subject, label, page, size);
        log.info(JSON.toJSONString(resultList));
    }

    @Test
    public void getUrisFromKeywordTest() {
        String keyword = "main";
        String subject = "edukg_cls_chemistry__main-C0";
        String label = "edukg_cls_chemistry__main-C0";
        List<String> resultList = neoManager.getUrisFromKeyword(keyword, subject, label);
        log.info(JSON.toJSONString(resultList));
    }

    @Test
    public void findPathBetweenNodesTest() {
        String head = "http://edukg.org/knowledge/3.0/instance/history#main-E8916";
        String tail = "http://edukg.org/knowledge/3.0/instance/history#main-E6598";
        Integer maxJumpTime = 5;
        List<Relation> resultList = neoManager.findPathBetweenNodes(head, tail, maxJumpTime);
        log.info(JSON.toJSONString(resultList));
    }

    @Test
    public void getEntityWithScoreFromProperty() {
        String name = "http";
        List<EntityWithScore> entityWithScores = neoManager.getEntityWithScoreFromProperty(name);
        log.info(JSON.toJSONString(entityWithScores));
    }

    @Test
    public void getEntityWithScoreFromName() {
        String name = "太白";
        List<EntityWithScore> entityWithScores = neoManager.getEntityWithScoreFromName(name);
        log.info(JSON.toJSONString(entityWithScores));
    }

    @Test
    public void getEntityFromUri() {
        String uri = "http://edukg.org/knowledge/3.0/instance/history#main-E8916";
        Entity entity = neoManager.getEntityFromUri(uri);
        log.info(JSON.toJSONString(entity));
    }

    @Test
    public void checkCommonP10() {
        String prop = "edukg_prop_common__main-P10";
        List<EntityWithScore> getAll = getPropertyAll(prop);
        List<EntityWithScore> getHttp = getPropertyHttp(prop);
        getAll.removeAll(getHttp);
        analyseConcrate(getAll);
        System.out.println(1);
    }

    @Test
    public void checkCommonR11() {
        String prop = "edukg_prop_biology__main-P92";
        List<EntityWithScore> getAll = getPropertyAll(prop);
        List<EntityWithScore> getHttp = getPropertyHttp(prop);
        getAll.removeAll(getHttp);
        analyseConcrate(getAll);
        System.out.println(1);
    }

    @Test
    public void checkHttpProper() {
        String name = "http";
        List<EntityWithScore> entityWithScores = getEntityWithScoreFromProperty(name);
        List<EntityWithScore> xlores = getEntityWithScoreFromPropertyXlore(name);
        entityWithScores.removeAll(xlores);
        Map<String, List<EntityWithScore>> m = entityWithScores.stream().collect(Collectors.groupingBy(e -> {
            String ret = "";
            try {
                ret = e.getAbstractMsg().split("http://")[1].split("/")[0];
            }
            catch (Exception ee) {
                ret = e.getAbstractMsg();
            }
            return ret;
        }));
        analyseConcrate(m.get("kb.cs.tsinghua.edu.cn"));
        analyseConcrate(m.get("edukg.org"));
        System.out.println(1);
    }

    public void analyseConcrate(List<EntityWithScore> entityList) {
        for(EntityWithScore entity : entityList) {
            for(String l : entity.getClassList()) {
                if(l.contains("C0")) {
                    entity.setUri(l);
                }
            }
        }
        Map<String, List<EntityWithScore>> m = entityList.stream().collect(Collectors.groupingBy(e -> e.getUri()));
        System.out.println(1);
        Map<String, List<EntityWithScore>> m2 = entityList.stream().collect(Collectors.groupingBy(e -> e.getK1()));
        System.out.println(2);
    }

    public List<EntityWithScore> getEntityWithScoreFromProperty(String name) {
        List<EntityWithScore> retList = new ArrayList<>();
        String query = "match(n:Resource)" +
                " where any(k in keys(n) where n[k] contains \"%s\")" +
                " with n, keys(n) as ks" +
                " unwind ks as k1" +
                " match(n)" +
                " where n[k1] contains \"%s\" and k1 starts with \"edukg\"" +
                " return n.rdfs__label as name, n.uri as uri, labels(n) as labels, n[k1] as v, k1 as k1" +
                "";
        query = String.format(query, name, name);
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            retList.add(EntityWithScore.builder()
                    .name((String) m.get("name"))
                    .uri((String) m.get("uri"))
                    .classList(Arrays.asList((String[]) m.get("labels")))
                    .abstractMsg((String) m.get("v"))
                    .k1((String) m.get("k1"))
                    .build());
        }
        retList = retList.stream().sorted(Comparator.comparingInt(EntityWithScore::getScore)).collect(Collectors.toList());
        return retList.stream().distinct().collect(Collectors.toList());
    }

    public List<EntityWithScore> getEntityWithScoreFromPropertyXlore(String name) {
        List<EntityWithScore> retList = new ArrayList<>();
        String query = "match(n:Resource)" +
                " where any(k in keys(n) where n[k] contains \"%s\")" +
                " with n, keys(n) as ks" +
                " unwind ks as k1" +
                " match(n)" +
                " where n[k1] contains \"%s\" and k1 starts with \"edukg\" and k1 contains \"xlore\"" +
                " return n.rdfs__label as name, n.uri as uri, labels(n) as labels, n[k1] as v, k1 as k1" +
                "";
        query = String.format(query, name, name);
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            retList.add(EntityWithScore.builder()
                    .name((String) m.get("name"))
                    .uri((String) m.get("uri"))
                    .classList(Arrays.asList((String[]) m.get("labels")))
                    .abstractMsg((String) m.get("v"))
                    .k1((String) m.get("k1"))
                    .build());
        }
        retList = retList.stream().sorted(Comparator.comparingInt(EntityWithScore::getScore)).collect(Collectors.toList());
        return retList.stream().distinct().collect(Collectors.toList());
    }

    public List<EntityWithScore> getPropertyHttp(String name) {
        List<EntityWithScore> retList = new ArrayList<>();
        String query = "match (n:Resource) where n.`" + name + "`=~ \".*http.*\"" +
                " return n.rdfs__label as name, n.uri as uri, labels(n) as labels, n.`" + name + "` as v";
        query = String.format(query, name, name);
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            retList.add(EntityWithScore.builder()
                    .name((String) m.get("name"))
                    .uri((String) m.get("uri"))
                    .classList(Arrays.asList((String[]) m.get("labels")))
                    .abstractMsg((String) m.get("v"))
                    .build());
        }
        retList = retList.stream().sorted(Comparator.comparingInt(EntityWithScore::getScore)).collect(Collectors.toList());
        return retList.stream().distinct().collect(Collectors.toList());
    }

    public List<EntityWithScore> getPropertyAll(String name) {
        List<EntityWithScore> retList = new ArrayList<>();
        String query = "match (n:Resource) where n.`" + name +"` is not null" +
                " return n.rdfs__label as name, n.uri as uri, labels(n) as labels, n.`" + name + "` as v";
        query = String.format(query, name, name);
        Result result = session.query(query, new HashMap<>());
        for (Map<String, Object> m : result.queryResults()) {
            retList.add(EntityWithScore.builder()
                    .name((String) m.get("name"))
                    .uri((String) m.get("uri"))
                    .classList(Arrays.asList((String[]) m.get("labels")))
                    .abstractMsg((String) m.get("v"))
                    .build());
        }
        retList = retList.stream().sorted(Comparator.comparingInt(EntityWithScore::getScore)).collect(Collectors.toList());
        return retList.stream().distinct().collect(Collectors.toList());
    }

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

    @Test
    public void combineEntity() throws IOException {
        String query;
        Result result;
        File file = new File("./nameToUri.txt");
        File failedRecord = new File("./failedRecord.txt");
        FileWriter fileWritter = new FileWriter(failedRecord.getName(),true);
        StringBuilder sb = new StringBuilder();
        List<String> contents = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String content : contents) {
            try {
                String name = content.split(" ")[0];
                String remainUri = content.split(" ")[1];
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
                for (Map<String, Object> m : result.queryResults()) {
                    String uri = (String) m.get("uri");
                    Entity entity = neoManager.getEntityFromUri(uri);
                    if(uri.equals(remainUri)) {
                        mainPropertyList.addAll(entity.getProperty());
                        mainRelationList.addAll(entity.getRelation());
                    }
                    else {
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
                List<Property> needAddProperty = combineProperty(mainPropertyList, propertyList);
                List<Relation> needAddRelation = combineRelation(mainRelationList, relationList);
                Map<String, String> reflectionMap = readUriReflection();
                updateRelation(reflectionMap, needAddRelation);
                List<Relation> oldRelation = new ArrayList<>(mainRelationList);
                updateRelation(reflectionMap, mainRelationList);
                for(Property property : needAddProperty) {
                    neoManager.updateProperty(remainUri, null, property);
                }
                for(int i=0; i<oldRelation.size(); i++) {
                    neoManager.updateRelation(oldRelation.get(i), mainRelationList.get(i));
                }
                for(Relation relation : needAddRelation) {
                    neoManager.updateRelation(null, relation);
                }
                neoManager.updateLabels(remainUri, labels);
            }
            catch (Exception e) {
                sb.append(content + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    public void updateRelation(Map<String, String> reflectionMap, List<Relation> relationList) {
        for(Relation relation : relationList) {
            String subjectUri = reflectionMap.get(relation.getSubjectUri()) == null ? relation.getSubjectUri() : reflectionMap.get(relation.getSubjectUri());
            String objectUri = reflectionMap.get(relation.getObjectUri()) == null ? relation.getObjectUri() : reflectionMap.get(relation.getObjectUri());
            relation.setSubjectUri(subjectUri);
            relation.setObjectUri(objectUri);
        }
    }

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

    public List<Property> combineProperty(List<Property> mainProperties, List<Property> propertyList) {
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

    public List<Relation> combineRelation(List<Relation> mainRelations, List<Relation> relationList) {
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
            }
            if(needAdd) {
                needAddRelation.add(relation);
            }
        }
        return needAddRelation;
    }

    public Map<String, String> readUriReflection() {
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
