package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.*;
import com.tsinghua.edukg.model.params.SearchSubgraphParam;
import com.tsinghua.edukg.service.GraphService;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.RuleHandler;
import lombok.extern.slf4j.Slf4j;
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
    public void analyseNodes() throws IOException {
        List<String> uriList = CommonUtil.readTextFromPath("./recordUri.txt");
        File file1 = new File("./out/relationWeakNodes.txt");
        File file2 = new File("./out/propWeakNodes.txt");
        FileWriter fileWriter1 = new FileWriter(file1.getName());
        FileWriter fileWriter2 = new FileWriter(file2.getName());
        int progress = 0;
        int relationFileCount = 0;
        int propFileCount = 0;
        for(String uri : uriList) {
            progress++;
            System.out.println("progress = " + progress);
            if(propFileCount > 10000) {
                file2 = new File("./propWeakNodes" + progress + ".txt");
                fileWriter2.close();
                fileWriter2 = new FileWriter(file2.getName());
                propFileCount = 0;
            }
            if(relationFileCount > 10000) {
                file1 = new File("./relationWeakNodes" + progress + ".txt");
                fileWriter1.close();
                fileWriter1 = new FileWriter(file1.getName());
                relationFileCount = 0;
            }
            Entity entity = neoManager.getEntityFromUri(uri);
            if(entity.getUri() == null) {
                continue;
            }
            if(entity.getProperty().size() < 3) {
                propFileCount += writeWeakNodes(fileWriter2, entity);
            }
            if(entity.getRelation().size() == 0) {
                relationFileCount += writeWeakNodes(fileWriter1, entity);
            }
        }
        fileWriter1.close();
        fileWriter2.close();
    }

    public Integer writeWeakNodes(FileWriter fileWriter, Entity entity) throws IOException {
        String needWrite = "";
        needWrite += "uri=" + entity.getUri() + " " + "name=" + entity.getName() + "\n";
        int acc = 2;
        for(Property property : entity.getProperty()) {
            acc += 2;
            needWrite += "propName=" + property.getPredicateLabel() + " " + "objectName=" + property.getObject() + "\n";
        }
        for(Relation relation : entity.getRelation()) {
            acc += 2;
            needWrite += "relationName=" + relation.getPredicateLabel() + " " + "subject=" + relation.getSubject() + " " + "object=" + relation.getObject() + "\n";
        }
        fileWriter.write(needWrite + "\n");
        fileWriter.flush();
        return acc;
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
