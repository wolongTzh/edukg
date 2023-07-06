package com.tsinghua.edukg.tools;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.ClassInternal;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.excel.Cls2Pred;
import com.tsinghua.edukg.model.excel.DomainRange;
import com.tsinghua.edukg.model.excel.TestSourceEntity;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.RuleHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;


@SpringBootTest
@Slf4j
public class DomainRangeTest {
    @Resource
    NeoManager neoManager;

    @Test
    public void cls2PredGen() throws IOException, CloneNotSupportedException {
        Map<String, String> clsMap = RuleHandler.grepClassOfAbbrMap();
        List<String> subjectList = Arrays.asList("chinese", "geo", "biology", "english", "history", "math", "physics", "politics", "chemistry", "common");
        ExcelWriter excelWriter = EasyExcel.write("./cls2pred.xlsx", Cls2Pred.class).build();
        ExcelWriter excelWriter2 = EasyExcel.write("./domainRange.xlsx", DomainRange.class).build();
        Map<String, WriteSheet> sheets = new HashMap<>();
        Map<String, WriteSheet> sheets2 = new HashMap<>();
        Map<String, List<Cls2Pred>> excelOut = new HashMap<>();
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
                        // 遍历每一个subject实体具有的cls
                        String subjectClsList = "";
                        for(ClassInternal classInternal : entityCon.getClassList()) {
                            if (StringUtils.isEmpty(classInternal.getLabel())) {
                                continue;
                            }
                            subjectClsList += "," + classInternal.getLabel();
                        }
                        subjectClsList = subjectClsList.substring(1);
                        List<Cls2Pred> cls2PredList = excelOut.get(subject);
                        Cls2Pred cls2Pred = Cls2Pred.builder()
                                .pred(property.getPredicateLabel() + " " + property.getPredicate())
                                .cls(clsName)
                                .example("subject=" + property.getSubject() + "，subjectCls=" + subjectClsList + "，predicate=" + property.getPredicateLabel() + "，object=" + property.getObject())
                                .predOrRelation("属性")
                                .build();
                        // 不希望在后面的每一行都把cls的名字打一遍
                        if(!firstTag) {
                            cls2Pred.setCls(null);
                        }
                        firstTag = false;
                        cls2PredList.add(cls2Pred);
                        predMap.put(property.getPredicate(), property.getPredicateLabel());
                    }
                }
                for(Relation relation : entityCon.getRelation()) {
                    if(!StringUtils.isEmpty(relation.getPredicateLabel()) && (relation.getPredicate().contains(subject) || relation.getPredicate().contains("common")) && !predMap.containsKey(
                            relation.getPredicate())) {
                        // 当前entity不是relation的subject，则跳过
                        if(!entityCon.getUri().equals(relation.getSubjectUri())) {
                            continue;
                        }
                        com.tsinghua.edukg.model.Entity objectEntity = neoManager.getEntityFromUri(relation.getObjectUri());
                        // 生成subject和object的classlist
                        String subjectClsList = "";
                        String objectClsList = "";
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
                        List<Cls2Pred> cls2PredList = excelOut.get(subject);
                        Cls2Pred cls2Pred = Cls2Pred.builder()
                                .pred(relation.getPredicateLabel() + " " + relation.getPredicate())
                                .cls(clsName)
                                .example("subject=" + relation.getSubject() + "，subjectCls=" + subjectClsList + "，predicate=" + relation.getPredicateLabel() + "，object=" + relation.getObject() + "，objectCls=" + objectClsList)
                                .predOrRelation("关系")
                                .build();
                        // 不希望在后面的每一行都把cls的名字打一遍
                        if(!firstTag) {
                            cls2Pred.setCls(null);
                        }
                        firstTag = false;
                        cls2PredList.add(cls2Pred);
                        predMap.put(relation.getPredicate(), relation.getPredicateLabel());
                    }
                }
            }
        }
        for(String subject : subjectList) {
            // 根据cls分map
            List<Cls2Pred> cls2PredList = excelOut.get(subject);
            List<DomainRange> domainRangeList = excelOut2.get(subject);
            Map<String, List<Cls2Pred>> clsPredMap = new HashMap<>();
            String curCls = "";
            List<Cls2Pred> cls2PredListNew = new ArrayList<>();
            for(Cls2Pred cls2Pred : cls2PredList) {
                if(!StringUtils.isEmpty(cls2Pred.getCls())) {
                    if(!curCls.equals("")) {
                        clsPredMap.put(curCls, cls2PredListNew);
                    }
                    curCls = cls2Pred.getCls();
                    cls2PredListNew = new ArrayList<>();
                }
                cls2PredListNew.add(cls2Pred);
            }
            if(cls2PredListNew.size() != 0) {
                clsPredMap.put(curCls, cls2PredListNew);
            }
            if(!subject.equals("common")) {
                // 通过上级class补充为空的本级class
                JSONObject root = CommonUtil.readJsonOut("./" + subject + ".json");
                parentHelper(clsPredMap, root);
            }
            List<Cls2Pred> cls2PredListFinal = new ArrayList<>();
            for(Map.Entry entry : clsPredMap.entrySet()) {
                List<Cls2Pred> cls2PredList1 = (List<Cls2Pred>) entry.getValue();
                cls2PredListFinal.addAll(cls2PredList1);
            }
            excelOut.put(subject, cls2PredListFinal);
            // 生成domain range excel
            domainRangeGen(clsPredMap, domainRangeList, subject);
            // 写入excel
            excelWriter.write(excelOut.get(subject), sheets.get(subject));
            excelWriter2.write(excelOut2.get(subject), sheets2.get(subject));
        }
        excelWriter.finish();
        excelWriter2.finish();
    }
    @Test
    public void readAndGenDomain() {
        String path = "./cls2pred.xlsx";
        String subject = "chinese";
        ExcelWriter excelWriter2 = EasyExcel.write("./domainRange.xlsx", DomainRange.class).build();
        WriteSheet sheet2 = EasyExcel.writerSheet(subject).build();
        List<Cls2Pred> cls2PredList = EasyExcel.read(path).head(Cls2Pred.class).sheet(subject).doReadSync();
        Map<String, List<Cls2Pred>> clsPredMap = new HashMap<>();
        List<DomainRange> domainRangeList = new ArrayList<>();
        List<Cls2Pred> cls2PredListNew = new ArrayList<>();
        String curCls = "";
        for(Cls2Pred cls2Pred : cls2PredList) {
            if(!StringUtils.isEmpty(cls2Pred.getCls())) {
                if(!curCls.equals("")) {
                    clsPredMap.put(curCls, cls2PredListNew);
                }
                curCls = cls2Pred.getCls();
                if(curCls.contains("from")) {
                    curCls = curCls.split(" from")[0];
                }
                cls2PredListNew = new ArrayList<>();
            }
            cls2PredListNew.add(cls2Pred);
        }
        if(cls2PredListNew.size() != 0) {
            clsPredMap.put(curCls, cls2PredListNew);
        }
        List<Cls2Pred> cls2PredListFinal = new ArrayList<>();
        for(Map.Entry entry : clsPredMap.entrySet()) {
            List<Cls2Pred> cls2PredList1 = (List<Cls2Pred>) entry.getValue();
            cls2PredListFinal.addAll(cls2PredList1);
        }
        domainRangeGen(clsPredMap, domainRangeList, subject);
        excelWriter2.write(domainRangeList, sheet2);
        excelWriter2.finish();
    }

    public void domainRangeGen(Map<String, List<Cls2Pred>> cls2PredMap, List<DomainRange> domainRangeList, String subject) {
        Map<String, DomainRange> predDomainRangeMap = new HashMap<>();
        for(Map.Entry entry : cls2PredMap.entrySet()) {
            List<Cls2Pred> cls2PredList = (List<Cls2Pred>) entry.getValue();
            String curCls = (String) entry.getKey();
            String examplePost = "";
            for(Cls2Pred cls2Pred : cls2PredList) {
                if(!StringUtils.isEmpty(cls2Pred.getCls()) && cls2Pred.getCls().contains("from")) {
                    examplePost = " parent：" + cls2Pred.getCls().split("父级clss：")[1] + " child：" + cls2Pred.getCls().split(" from")[0];
                }
                String pred = cls2Pred.getPred();
                if(!pred.contains(subject)) {
                    continue;
                }
                String example = cls2Pred.getExample() + examplePost;
                DomainRange domainRange = new DomainRange();
                // map中已经包含该谓词的信息了
                if(predDomainRangeMap.containsKey(pred)) {
                    domainRange = predDomainRangeMap.get(pred);
                }
                // map中未包含该谓词的信息
                else {
                    domainRange = DomainRange.builder()
                            .domain("")
                            .range("")
                            .example("")
                            .pred(pred)
                            .predOrRelation(cls2Pred.getPredOrRelation())
                            .build();
                }
                String domain = domainRange.getDomain();
                String range = domainRange.getRange();
                String domainExample = domainRange.getExample();
                // 当前pred为属性
                if(cls2Pred.getPredOrRelation().equals("属性")) {
                    if(!domain.equals("")) {
                        domain += ",";
                    }
                    domain += curCls;
                }
                // 当前pred为关系
                else {
                    // 需要添加domain的情况
                    if(example.split("subjectCls=")[1].split("，predicate")[0].contains(curCls)) {
                        if(!domain.equals("")) {
                            domain += ",";
                        }
                        domain += curCls;
                    }
                    // 需要添加range的情况
                    if(example.split("objectCls=")[1].contains(curCls)) {
                        if(!range.equals("")) {
                            range += ",";
                        }
                        range += curCls;
                    }
                }
                if(!domainExample.equals("")) {
                    domainExample += "\n";
                }
                domainExample += example;
                domainRange.setDomain(domain);
                domainRange.setRange(range);
                domainRange.setExample(domainExample);
                predDomainRangeMap.put(pred, domainRange);
            }
        }
        for(Map.Entry domainEntry : predDomainRangeMap.entrySet()) {
            domainRangeList.add((DomainRange) domainEntry.getValue());
        }
    }

    public void parentHelper(Map<String, List<Cls2Pred>> cls2PredMap, JSONObject root) throws CloneNotSupportedException {
        if(cls2PredMap.entrySet().size() == 0) {
            return;
        }
        String name = root.getString("title");
        if(!root.containsKey("children")) {
            return;
        }
        JSONArray children = root.getJSONArray("children");
        for(int i=0; i<children.size(); i++) {
            JSONObject child = children.getJSONObject(i);
            String childName = child.getString("title");
            if(!cls2PredMap.containsKey(childName)) {
                List<Cls2Pred> cls2PredList = cls2PredMap.get(name);
                List<Cls2Pred> cls2PredListNew = new ArrayList<>();
                boolean startTag = true;
                for(Cls2Pred cls2Pred : cls2PredList) {
                    Cls2Pred cls2PredNew = cls2Pred.clone();
                    if(startTag) {
                        cls2PredNew.setCls(childName + " from 父级clss：" + name);
                        startTag = false;
                    }
                    cls2PredListNew.add(cls2PredNew);
                }
                cls2PredMap.put(childName, cls2PredListNew);
            }
            parentHelper(cls2PredMap, child);
        }
    }
}
