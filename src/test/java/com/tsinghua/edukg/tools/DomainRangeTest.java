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
                System.out.println("cur entity is : " + entity.getName() + " " + entity.getUri());
                com.tsinghua.edukg.model.Entity entityCon = neoManager.getEntityFromUri(entity.getUri());
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
                    if(!StringUtils.isEmpty(relation.getPredicateLabel()) && relation.getPredicate().contains("chinese") && !predMap.containsKey(
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
                            com.tsinghua.edukg.model.Entity tempEntity = neoManager.getEntityFromUri(relation.getObjectUri());
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
                                if(!StringUtils.isEmpty(range) && !range.contains(classInternal.getLabel())) {
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
}
