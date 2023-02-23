package com.tsinghua.edukg.tools;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.manager.NeoManager;
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
                    if(nameSet.contains(prefix) && !RuleHandler.getPropertyAbbrByName(null, suffix).equals("")) {
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
                    if (nameSet.contains(prefix) && StringUtils.isEmpty(RuleHandler.getPropertyAbbrByName(null, suffix))) {
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
}
