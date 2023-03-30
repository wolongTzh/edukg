package com.tsinghua.edukg.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.ClassInternal;
import com.tsinghua.edukg.model.ClassTree;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.Property;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootTest
@Slf4j
public class AnalyzerTool {

    @Resource
    NeoManager neoManager;

    @Autowired
    @Qualifier("neo4jSession")
    Session session;

    static JSONObject merged = new JSONObject();

    static List<String> needRemove = new ArrayList<>();

    /**
     * 长实体名实体分析工具
     * @throws IOException
     */
    @Test
    public void analyzeLongName() throws IOException {
        String query = "match (n:Resource)" +
                " return n.rdfs__label as name";
        Result result = session.query(query, new HashMap<>());
        List<String> temp = new ArrayList<>();
        Map<String, Integer> statisticMap = new HashMap<>();
        Set<String> nameSet = new HashSet<>();
        File fileOut =new File("./longNameJson.txt");
        if(!fileOut.exists()){
            fileOut.createNewFile();
        }
        FileWriter fileWritter = new FileWriter(fileOut.getName(),true);
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> m : result.queryResults()) {
            try {
                String name = (String) m.get("name");
                nameSet.add(name);
                if(!StringUtils.isEmpty(name) && name.length() >= 10) {
//                    List<Entity> entityList = neoManager.getEntityListFromName(name);
                    temp.add(name);
                }
//                    for(Entity entity : entityList) {
//                        Entity concrateEntity = neoManager.getEntityFromUri(entity.getUri());
//                        sb.append(JSON.toJSONString(concrateEntity) + "\n\n");
//                        for(Relation relation : concrateEntity.getRelation()) {
//                            statisticMap.putIfAbsent(relation.getPredicateLabel(), 0);
//                            statisticMap.put(relation.getPredicate(), statisticMap.get(relation.getPredicate()) + 1);
//                        }
//                    }
//                }
            }
            catch (Exception e) {
                continue;
            }
        }
        for (Map<String, Object> m : result.queryResults()) {
            try {
                String name = (String) m.get("name");
                if(name.split("的").length == 2) {
                    String prefix =  name.split("的")[0];
                    String suffix =  name.split("的")[1];
                    if(nameSet.contains(prefix) && !RuleHandler.getPropertyAbbrWithoutSubject(suffix).equals("")) {
                        sb.append(prefix + " " + suffix + "\n");
                        if(!StringUtils.isEmpty(name) && name.length() >= 10) {
                            List<Entity> entityList = neoManager.getEntityListFromName(name);
                            for (Entity entity : entityList) {
                                Entity concrateEntity = neoManager.getEntityFromUri(entity.getUri());
                                sb.append(JSON.toJSONString(concrateEntity) + "\n\n");
                            }
                        }
                    }
                }
//                if(!StringUtils.isEmpty(name) && name.length() >= 10) {
//                    List<Entity> entityList = neoManager.getEntityListFromName(name);
//                    for(Entity entity : entityList) {
//                        Entity concrateEntity = neoManager.getEntityFromUri(entity.getUri());
//                        sb.append(JSON.toJSONString(concrateEntity) + "\n\n");
//                        for(Relation relation : concrateEntity.getRelation()) {
//                            statisticMap.putIfAbsent(relation.getPredicateLabel(), 0);
//                            statisticMap.put(relation.getPredicate(), statisticMap.get(relation.getPredicate()) + 1);
//                        }
//                    }
//                }
            }
            catch (Exception e) {
                continue;
            }
        }

//        for(Map.Entry entry : statisticMap.entrySet()) {
//            log.info("关系名 : " + entry.getKey() + " & 关系出现次数 : " + entry.getValue());
//        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    /**
     * 分析模板合并：xx的xx还有没有其它可挖掘的地方（已乱）
     * @throws IOException
     */
    @Test
    public void combineTemplateEntity() throws IOException {
        String query = "match (n:Resource)" +
                " return n.rdfs__label as name";
        Result result = session.query(query, new HashMap<>());
        Set<String> nameSet = new HashSet<>();
        Map<String, Integer> notExistPredicate = new HashMap<>();
        File deleteUriRecord = new File("./predicateStatistic.txt");
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
//                    if(nameSet.contains(prefix) && StringUtils.isEmpty(RuleHandler.getPropertyAbbrByName(null, suffix))) {
//                        notExistPredicate.putIfAbsent(suffix, 0);
//                        notExistPredicate.put(suffix, notExistPredicate.get(suffix) + 1);
//                    }
                    if (nameSet.contains(prefix) && StringUtils.isEmpty(RuleHandler.getPropertyAbbrWithoutSubject(suffix))) {
                        if (!StringUtils.isEmpty(name)) {
                            List<Entity> entityList = neoManager.getEntityListFromName(name);
                            for(Entity entity : entityList) {
                                Entity concrateEntity = neoManager.getEntityFromUri(entity.getUri());
                                List<Property> propertyList = concrateEntity.getProperty();
                                RuleHandler.propertyConverter(propertyList);
                                for(Property property : propertyList) {
                                    if(property.getPredicateLabel().equals("内容") || property.getPredicateLabel().equals("包含")) {
//                                        List<Entity> targetEntityList = neoManager.getEntityListFromName(prefix);
//                                        Entity targetEntity = neoManager.getEntityFromUri(targetEntityList.get(0).getUri());
//                                        String temps = RuleHandler.getPropertyAbbrByName(null, suffix);
//                                        String predicate = RuleHandler.getPropertyNameByUri(temps);
//                                        Property propertyAdd = Property.builder()
//                                                .subject(prefix)
//                                                .object(property.getObject())
//                                                .predicate(property.getPredicate())
//                                                .build();
//                                        neoManager.updateProperty(targetEntity.getUri(), propertyAdd, null);
//                                        sb.append(entity.getUri() + " " + entity.getName() + "\n");
                                        notExistPredicate.putIfAbsent(suffix, 0);
                                        notExistPredicate.put(suffix, notExistPredicate.get(suffix) + 1);
                                        break;
                                    }
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
        Map<String, Integer> reusltMap = sortMapByValue(notExistPredicate);
        for(Map.Entry entry : reusltMap.entrySet()) {
            sb.append(entry.getKey() + " " + entry.getValue() + "\n");
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }
    @Test
    public void subClassJsonGen() throws IOException {
        Map<String, List<String>> subClassMap = RuleHandler.grepSubClassOfAbbrMap();
        List<String> subjects = Arrays.asList("chinese", "math", "english", "physics", "biology", "chemistry", "geo", "history", "politics");
        for(String subject : subjects) {
            String path = "./" + subject + "_tree.json";
            List<String> needRemove = new ArrayList<>();
            JSONObject fusionMap = mergeHierarchy(needRemove, subject, subClassMap, subClassMap);
            File conceptEntities = new File(path);
            FileWriter fileWritter = new FileWriter(conceptEntities.getName(),false);
            ClassTree classTree = transfer(fusionMap);
            fileWritter.write(JSON.toJSONString(classTree));
            fileWritter.flush();
            fileWritter.close();
        }
    }

    /**
     * 使用 Map按value进行排序
     * @param oriMap
     * @return
     */
    public Map<String, Integer> sortMapByValue(Map<String, Integer> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(
                oriMap.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();
        Map.Entry<String, Integer> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public static JSONObject mergeHierarchy( List<String> needRemove, String subject, Map<String, List<String>> hierarchy, Map<String, List<String>> hierarchyMap) {
        JSONObject merge = new JSONObject();

        for (String key : hierarchy.keySet()) {
            if(!key.contains(subject)) {
                continue;
            }
            List<String> needRemove1 = new ArrayList<>();
            List<String> value = hierarchy.get(key);
            if (value != null && !value.isEmpty()) {
                JSONObject mergedValue = new JSONObject();
                boolean start = true;
                for (String childKey : value) {
                    if(start) {
                        start = false;
                        continue;
                    }
                    if (hierarchyMap.containsKey(childKey)) {
                        Map<String, List<String>> childMap = new HashMap<>();
                        childMap.put(childKey, hierarchyMap.get(childKey));
                        JSONObject childValue = mergeHierarchy(needRemove1, subject, childMap, hierarchyMap);
                        needRemove.add(childKey);
                        if (childValue.get(childKey) == null) {
                            continue;
                        }
                        else if (childValue.getJSONObject(childKey).size() != 0) {
                            mergedValue.put(childKey, childValue.get(childKey));
                            for(Map.Entry<String, Object> entry : childValue.entrySet()) {
                                needRemove.add(entry.getKey());
                            }
                        }
                        else {
                            mergedValue.put(childKey, RuleHandler.getClassNameByAbbr(childKey));
                        }
                    } else {
                        mergedValue.put(childKey, ClassInternal.builder()
                                        .label(RuleHandler.getClassNameByAbbr(childKey))
                                        .id(RuleHandler.getClassNameByAbbr(childKey))
                                        .build());
                    }
                }
                merge.put(key, mergedValue);
            } else {
                merge.put(key, ClassInternal.builder()
                        .label(RuleHandler.getClassNameByAbbr(key))
                        .id(RuleHandler.getClassNameByAbbr(key))
                        .build());

            }
        }
        if(hierarchy.size() == hierarchyMap.size()) {
            System.out.println(1);
        }
        for(String del : needRemove) {
            merge.remove(del);
        }
        return merge;
    }

    public static ClassTree transfer(JSONObject jsonObject) {
        String outKey = jsonObject.entrySet().iterator().next().getKey();
        List<ClassTree> classTreeList = new ArrayList<>();
        ClassTree head = ClassTree.builder()
                .id(outKey)
                .label(RuleHandler.getClassNameByAbbr(outKey))
                .childNodes(classTreeList)
                .build();
        Object inner = jsonObject.get(outKey);
        if(inner instanceof String) {
            return head;
        }
        Set<Map.Entry<String, Object>> entrySet = ((JSONObject) inner).entrySet();
        for(Map.Entry<String, Object> entry : entrySet) {
            Object value = entry.getValue();
            ClassTree classTree;
            if(value instanceof String) {
                classTree = ClassTree.builder()
                        .id(entry.getKey())
                        .label(RuleHandler.getClassNameByAbbr(entry.getKey()))
                        .childNodes(null)
                        .build();
            }
            else {
                JSONObject handle = new JSONObject();
                handle.put(entry.getKey(), value);
                classTree = transfer(handle);
            }
            classTreeList.add(classTree);
        }
        return head;
    }
}
