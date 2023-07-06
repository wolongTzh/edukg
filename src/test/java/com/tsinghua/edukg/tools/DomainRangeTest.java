package com.tsinghua.edukg.tools;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.ClassInternal;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.excel.Cls2Pred;
import com.tsinghua.edukg.model.excel.DomainRange;
import com.tsinghua.edukg.utils.RuleHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;


@SpringBootTest
@Slf4j
public class DomainRangeTest {
    @Resource
    NeoManager neoManager;

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
            if(!((String) entry.getKey()).contains("chinese")) {
                continue;
            }
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
            List<com.tsinghua.edukg.model.Entity> entityList = neoManager.getEntityListFromClass(clsCode);
            for(com.tsinghua.edukg.model.Entity entity : entityList) {
                com.tsinghua.edukg.model.Entity entityCon = neoManager.getEntityFromUri(entity.getUri());
                for(Property property : entityCon.getProperty()) {
                    if(!StringUtils.isEmpty(property.getPredicateLabel()) && (property.getPredicate().contains(subject) || property.getPredicate().contains("common") || property.getPredicate().contains("rdfs")) && !predMap.containsKey(property.getPredicate())) {
                        List<Cls2Pred> cls2PredList = excelOut.get(subject);
                        // 不希望在后面的每一行都把cls的名字打一遍
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
                        // 该属性谓词之前处理过
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
                        // 该属性谓词之前未被处理过
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
                    if(relation.getSubject().equals("《诗经》") && relation.getObject().equals("一") && clsName.equals("汉字")) {
                        System.out.println(1);
                    }
                    if(!StringUtils.isEmpty(relation.getPredicateLabel()) && (relation.getPredicate().contains(subject) || relation.getPredicate().contains("common")) && !predMap.containsKey(
                            relation.getPredicate())) {
                        List<Cls2Pred> cls2PredList = excelOut.get(subject);
                        // 不希望在后面的每一行都把cls的名字打一遍
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
                        DomainRange domainRange = new DomainRange();
                        // 该关系谓词之前处理过
                        if(domainRangeMap.containsKey(relation.getPredicate())) {
                            domainRange = domainRangeMap.get(relation.getPredicate());
                        }
                        // 该关系谓词之前未被处理过
                        else {
                            domainRange = DomainRange.builder()
                                    .pred(relation.getPredicateLabel() + " " + relation.getPredicate())
                                    .domain("")
                                    .range("")
                                    .predOrRelation("关系")
                                    .build();
                        }
                        // 进行domainRange对象装填
                        String domain = domainRange.getDomain();
                        String range = domainRange.getRange();
                        com.tsinghua.edukg.model.Entity objectEntity = neoManager.getEntityFromUri(relation.getObjectUri());
                        String subjectClsList = "";
                        String objectClsList = "";
                        boolean domainAdd = false;
                        boolean rangeAdd = false;
                        // 遍历每一个subject实体具有的cls
                        for(ClassInternal classInternal : entityCon.getClassList()) {
                            if (StringUtils.isEmpty(classInternal.getLabel())) {
                                continue;
                            }
                            subjectClsList += "," + classInternal.getLabel();
                        }
                        subjectClsList = subjectClsList.substring(1);
                        // 遍历每一个object实体具有的cls
                        for(ClassInternal classInternal : objectEntity.getClassList()) {
                            if (StringUtils.isEmpty(classInternal.getLabel())) {
                                continue;
                            }
                            objectClsList += "," + classInternal.getLabel();
                        }
                        objectClsList = objectClsList.substring(1);
                        // 如果subject实体包含了当前的class
                        if(subjectClsList.contains(clsName)) {
                            // domain如果包含过了当前正在处理的cls，就不用再添加一次了
                            if(!domain.contains(clsName)) {
                                domainAdd = true;
                                domain += "," + clsName;
                                domainRange.setDomain(domain);
                            }
                        }
                        // 如果object实体包含了当前的class
                        if(objectClsList.contains(clsName)) {
                            // range如果包含过了当前正在处理的cls，就不用再添加一次了
                            if(!StringUtils.isEmpty(range) && !range.contains(clsName)) {
                                rangeAdd = true;
                                range += "," + clsName;
                                domainRange.setRange(range);
                            }
                        }
                        // 判断example是否需要添加，如果添加了新的domain或range才需要添加
                        if(domainAdd || rangeAdd) {
                            String example = domainRange.getExample();
                            if(domainAdd && rangeAdd) {
                                example += "subject=" + relation.getSubject() + "，subjectCls=" + clsName + "，predicate=" + relation.getPredicateLabel() + "，object=" + relation.getObject() + "，objectCls=" + clsName + "\n";
                            }
                            else if(domainAdd) {
                                example += "subject=" + relation.getSubject() + "，subjectCls=" + clsName + "，predicate=" + relation.getPredicateLabel() + "，object=" + relation.getObject() + "，objectCls=" + objectClsList + "\n";
                            }
                            else if(rangeAdd) {
                                example += "subject=" + relation.getSubject() + "，subjectCls=" + subjectClsList + "，predicate=" + relation.getPredicateLabel() + "，object=" + relation.getObject() + "，objectCls=" + clsName + "\n";
                            }
                            domainRange.setExample(example);
                        }
                        // 之前未处理过该谓词，加入map
                        if(!domainRangeMap.containsKey(relation.getPredicate())) {
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
}
