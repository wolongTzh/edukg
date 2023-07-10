package com.tsinghua.edukg;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.*;
import com.tsinghua.edukg.model.excel.Cls2Pred;
import com.tsinghua.edukg.model.excel.DomainRange;
import com.tsinghua.edukg.model.excel.TestOutEntity;
import com.tsinghua.edukg.model.params.SearchSubgraphParam;
import com.tsinghua.edukg.service.GraphService;
import com.tsinghua.edukg.utils.RuleHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class NeoManagerTest {

    @Resource
    NeoManager neoManager;

    @Autowired
    GraphService graphService;

    @Autowired
    @Qualifier("neo4jSession")
    Session session;

    @Test
    public void removeRelation() {
        Relation pre = Relation.builder()
                .predicate("edukg_prop_chinese__main-R2")
                .subjectUri("http://edukg.org/knowledge/3.0/instance/chinese#main-E6105")
                .objectUri("http://edukg.org/knowledge/3.0/instance/chinese#main-E4621")
                .build();
        neoManager.updateRelation(pre, null);
    }

    @Test
    public void updateLabels() {
        String uri = "http://edukg.org/knowledge/3.0/instance/chinese#main-E7392";
        List<String> labels = Arrays.asList("edukg_cls_chinese__main-C0", "edukg_cls_chinese__main-C24");
        neoManager.updateLabels(uri, labels);
    }

    @Test
    public void updateProp() {
        String uri = "http://edukg.org/knowledge/3.0/instance/chinese#main-E2093";
        Property pre = Property.builder()
                .subject("水调歌头")
                .predicate("edukg_prop_chinese__main-P12")
                .object("明月几时有？把酒问青天。不知天上宫阙（què），今夕是何年。我欲乘风归去，又恐琼楼玉宇，高处不胜寒。起舞弄清影，何似在人间。\\n转朱阁，低绮（qǐ）户，照无眠。不应有恨，何事长向别时圆？人有悲欢离合，月有阴晴圆缺，此事古难全。但愿人长久，千里共婵娟。")
                .build();
        Property post = Property.builder()
                .subject("水调歌头")
                .predicate("edukg_prop_chinese__main-P12")
                .object("人有悲欢离合，月有阴晴圆缺，此事古难全。但愿人长久，千里共婵娟。")
                .build();
        neoManager.updateProperty(uri, pre, post);
    }
    @Test
    public void domainRangeGen() {
        Map<String, String> clsMap = RuleHandler.grepClassOfAbbrMap();
        List<String> subjectList = Arrays.asList("chinese", "geo", "biology", "english", "history", "math", "physics", "politics", "chemistry", "common");
        ExcelWriter excelWriter = EasyExcel.write("./cls2pred.xlsx", Cls2Pred.class).build();
        ExcelWriter excelWriter2 = EasyExcel.write("./domainRange.xlsx", DomainRange.class).build();
        Map<String, WriteSheet> sheets = new HashMap<>();
        Map<String, WriteSheet> sheets2 = new HashMap<>();
        Map<String, List<Cls2Pred>> excelOut = new HashMap<>();
        Map<String, DomainRange> domainRangeMap = new HashMap<>();
        Map<String, List<DomainRange>> excelOut2 = new HashMap<>();
        for(String subject : subjectList) {
            excelOut.put(subject, new ArrayList<>());
            excelOut2.put(subject, new ArrayList<>());
            sheets.put(subject, EasyExcel.writerSheet(subject).build());
            sheets2.put(subject, EasyExcel.writerSheet(subject).build());
        }
        for(Map.Entry entry : clsMap.entrySet()) {
            System.out.println("cur cls is :" + entry.getKey() + "  " + entry.getValue());
            boolean firstTag = true;
            String clsName = (String) entry.getValue();
            String subject = "";
            for(String sub : subjectList) {
                if(((String) entry.getKey()).contains(sub)) {
                    subject = sub;
                    break;
                }
            }
            Map<String, String> predMap = new HashMap<>();
            String clsCode = RuleHandler.getLabelAbbrByUri((String) entry.getKey());
            List<Entity> entityList = neoManager.getEntityListFromClass(clsCode);
            for(Entity entity : entityList) {
                Entity entityCon = neoManager.getEntityFromUri(entity.getUri());
                for(Property property : entityCon.getProperty()) {
                    if(!StringUtils.isEmpty(property.getPredicateLabel()) && property.getPredicate().contains(subject) && !predMap.containsKey(property.getPredicate())) {
                        List<Cls2Pred> cls2PredList = excelOut.get(subject);
                        if(firstTag) {
                            cls2PredList.add(Cls2Pred.builder()
                                    .pred(property.getPredicateLabel() + " " + property.getPredicate())
                                    .cls(clsName)
                                    .example("subject=" + property.getSubject() + "，predicate=" + property.getPredicateLabel() + "，object=" + property.getObject())
                                    .predOrRelation("属性")
                                    .build());
                            firstTag = false;
                        }
                        else {
                            cls2PredList.add(Cls2Pred.builder()
                                    .pred(property.getPredicateLabel() + " " + property.getPredicate())
                                    .example("subject=" + property.getSubject() + "，predicate=" + property.getPredicateLabel() + "，object=" + property.getObject())
                                    .predOrRelation("属性")
                                    .build());
                        }
                        if(domainRangeMap.containsKey(property.getPredicate())) {
                            DomainRange domainRange = domainRangeMap.get(property.getPredicate());
                            String domain = domainRange.getDomain();
                            if(!domain.contains(clsName)) {
                                domain += "," + clsName;
                                domainRange.setDomain(domain);
                                String example = domainRange.getExample();
                                example += "subject=" + property.getSubject() + "，subjectCls=" + clsName + "，predicate=" + property.getPredicateLabel() + "，object=" + property.getObject() + "\n";
                                domainRange.setExample(example);
                            }
                        }
                        else {
                            domainRangeMap.put(property.getPredicate(), DomainRange.builder()
                                    .pred(property.getPredicateLabel() + " " + property.getPredicate())
                                    .domain(clsName)
                                    .predOrRelation("属性")
                                    .example("subject=" + property.getSubject() + "，subjectCls=" + clsName + "，predicate=" + property.getPredicateLabel() + "，object=" + property.getObject() + "\n")
                                    .build());
                        }
                        predMap.put(property.getPredicate(), property.getPredicateLabel());
                    }
                }
                for(Relation relation : entityCon.getRelation()) {
                    if(!StringUtils.isEmpty(relation.getPredicateLabel()) && relation.getPredicate().contains(subject) && !predMap.containsKey(
                            relation.getPredicate())) {
                        List<Cls2Pred> cls2PredList = excelOut.get(subject);
                        if(firstTag) {
                            cls2PredList.add(Cls2Pred.builder()
                                    .pred(relation.getPredicateLabel() + " " + relation.getPredicate())
                                    .cls(clsName)
                                    .example("subject=" + relation.getSubject() + "，predicate=" + relation.getPredicateLabel() + "，object=" + relation.getObject())
                                    .predOrRelation("关系")
                                    .build());
                            firstTag = false;
                        }
                        else {
                            cls2PredList.add(Cls2Pred.builder()
                                    .pred(relation.getPredicateLabel() + " " + relation.getPredicate())
                                    .example("subject=" + relation.getSubject() + "，predicate=" + relation.getPredicateLabel() + "，object=" + relation.getObject())
                                    .predOrRelation("关系")
                                    .build());
                        }
                        if(domainRangeMap.containsKey(relation.getPredicate())) {
                            DomainRange domainRange = domainRangeMap.get(relation.getPredicate());
                            String domain = domainRange.getDomain();
                            String range = domainRange.getRange();
                            Entity tempEntity = neoManager.getEntityFromUri(relation.getObjectUri());
                            String clsList = "";
                            boolean needAddExample = false;
                            if(!domain.contains(clsName)) {
                                needAddExample = true;
                                domain += "," + clsName;
                                domainRange.setDomain(domain);
                            }
                            for(ClassInternal classInternal : tempEntity.getClassList()) {
                                if(StringUtils.isEmpty(classInternal.getLabel())) {
                                    continue;
                                }
                                if(!range.contains(classInternal.getLabel())) {
                                    needAddExample = true;
                                    range += "," + classInternal.getLabel();
                                }
                                domainRange.setRange(range);
                            }
                            if(needAddExample) {
                                for(ClassInternal classInternal : tempEntity.getClassList()) {
                                    if(StringUtils.isEmpty(classInternal.getLabel())) {
                                        continue;
                                    }
                                    clsList += "," + classInternal.getLabel();
                                }
                                clsList = clsList.substring(1);
                                String example = domainRange.getExample();
                                example += "subject=" + relation.getSubject() + "，subjectCls=" + clsName + "，predicate=" + relation.getPredicateLabel() + "，object=" + relation.getObject() + "，objectCls=" + clsList + "\n";
                                domainRange.setExample(example);
                            }
                        }
                        else {
                            DomainRange domainRange = DomainRange.builder()
                                    .pred(relation.getPredicateLabel() + " " + relation.getPredicate())
                                    .domain(clsName)
                                    .predOrRelation("关系")
                                    .build();
                            Entity tempEntity = neoManager.getEntityFromUri(relation.getObjectUri());
                            String range = "";
                            for(ClassInternal classInternal : tempEntity.getClassList()) {
                                if(StringUtils.isEmpty(classInternal.getLabel())) {
                                    continue;
                                }
                                range += "," + classInternal.getLabel();
                            }
                            domainRange.setRange(range.substring(1));
                            String clsList = "";
                            for(ClassInternal classInternal : tempEntity.getClassList()) {
                                if(StringUtils.isEmpty(classInternal.getLabel())) {
                                    continue;
                                }
                                clsList += "," + classInternal.getLabel();
                            }
                            clsList = clsList.substring(1);
                            String example = "";
                            example += "subject=" + relation.getSubject() + "，subjectCls=" + clsName + "，predicate=" + relation.getPredicateLabel() + "，object=" + relation.getObject() + "，objectCls=" + clsList + "\n";
                            domainRange.setExample(example);
                            domainRangeMap.put(relation.getPredicate(), domainRange);
                        }
                        predMap.put(relation.getPredicate(), relation.getPredicateLabel());
                    }
                }
            }
        }
        for(Map.Entry entry : domainRangeMap.entrySet()) {
            String predCode = (String) entry.getKey();
            for(String subject : subjectList) {
                if(predCode.contains(subject)) {
                    List<DomainRange> domainRangeList = excelOut2.get(subject);
                    domainRangeList.add((DomainRange) entry.getValue());
                }
            }
        }
        for(String subject : subjectList) {
            excelWriter.write(excelOut.get(subject), sheets.get(subject));
            excelWriter2.write(excelOut2.get(subject), sheets2.get(subject));
        }
        excelWriter.finish();
        excelWriter2.finish();
    }

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
        String head = "http://edukg.org/knowledge/3.0/instance/geo#main-E63";
        String tail = "http://edukg.org/knowledge/3.0/instance/chemistry#main-E1538";
        Integer maxJumpTime = 5;
        List<Relation> resultList = neoManager.findPathBetweenNodes(head, tail, maxJumpTime);
        log.info(JSON.toJSONString(resultList));
    }

    @Test
    public void findPathBetweenNodesServiceTest() {
        String head = "http://edukg.org/knowledge/3.0/instance/geo#main-E63";
        String tail = "http://edukg.org/knowledge/3.0/instance/chemistry#main-E1538";
        Integer maxJumpTime = 5;
        List<String> insList = Arrays.asList(head, tail);
        SearchSubgraphParam searchSubgraphParam = new SearchSubgraphParam();
        searchSubgraphParam.setInstanceList(insList);
        List<Relation> resultList = graphService.searchSubgraph(searchSubgraphParam);
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
    public void getEntityListFromClass() {
        String className = "edukg_cls_chinese__main-C23";
        List<Entity> entityList = neoManager.getEntityListFromClass(className);
        log.info(JSON.toJSONString(entityList));
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
            for(ClassInternal l : entity.getClassList()) {
                if(l.getId().contains("C0")) {
                    entity.setUri(l.getId());
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
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels"))))
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
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels"))))
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
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels"))))
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
                    .classList(RuleHandler.classConverter(Arrays.asList((String[]) m.get("labels"))))
                    .abstractMsg((String) m.get("v"))
                    .build());
        }
        retList = retList.stream().sorted(Comparator.comparingInt(EntityWithScore::getScore)).collect(Collectors.toList());
        return retList.stream().distinct().collect(Collectors.toList());
    }
}
