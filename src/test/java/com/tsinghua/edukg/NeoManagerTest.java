package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.ClassInternal;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.EntityWithScore;
import com.tsinghua.edukg.model.Relation;
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

    @Test
    public void analyseNodes() {
        List<EntityWithScore> retList = new ArrayList<>();
        String query = "match (n) n.uri as uri return uri ";
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
