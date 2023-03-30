package com.tsinghua.edukg.tools;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.ClassInternal;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.excel.EntityAdderTemp;
import com.tsinghua.edukg.model.excel.TestOutEntity;
import com.tsinghua.edukg.model.excel.TestSourceEntity;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.RuleHandler;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class EntityCompletion {

    @Resource
    NeoManager neoManager;

    /**
     * 新增古诗
     * @throws IOException
     */
    @Test
    public void poetAdder() throws IOException {
        String path = "./add_poets.json";
        JSONArray array = CommonUtil.readJsonOut(path).getJSONArray("content");
//        List<Entity> entityList = new ArrayList<>();
//        for(int i=0; i<array.size(); i++) {
//            JSONObject jsonObject = array.getJSONObject(i);
//            entityList.add(Entity.builder()
//                    .property(genProperty(jsonObject))
//                    .build());
//        }
//        System.out.println(JSONArray.toJSONString(entityList));
//        neoManager.batchAddEntities("chinese", null, entityList);
        for(int i=0; i<array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            String name = (String) jsonObject.get("title");
            genRelation(jsonObject, name);
        }
    }

    List<Property> genProperty(JSONObject jsonObject) {
        List<Property> propertyList = new ArrayList<>();
        String name = (String) jsonObject.get("title");
        String content = (String) jsonObject.get("content");
        propertyList.add(Property.builder()
                .predicate("rdfs__label")
                .subject(name)
                .object(name)
                .build());
        propertyList.add(Property.builder()
                .predicate("edukg_prop_common__main-P3")
                .subject(name)
                .object(content)
                .build());
        return propertyList;
    }

    void genRelation(JSONObject jsonObject, String name) {
        String author = (String) jsonObject.get("author");
        List<Entity> entityList = neoManager.getEntityListFromName(author);
        List<Entity> nameEntityList = neoManager.getEntityListFromName(name);
        if(nameEntityList.size() > 0) {
            Entity nameEntity = nameEntityList.get(0);
            if(entityList != null && entityList.size() >= 1) {
                Entity entity = entityList.get(0);
                Relation relation = Relation.builder()
                        .subject(name)
                        .subjectUri(nameEntity.getUri())
                        .object(author)
                        .objectUri(entity.getUri())
                        .predicate("edukg_prop_chinese__main-R2")
                        .build();
                System.out.println(JSONArray.toJSONString(relation));
                neoManager.updateRelation(null, relation);
            }
        }
    }

    /**
     * 分析kbqa给出的谓词top1
      */
    @Test
    public void missingAdder() {
        String path = "./output.xlsx";
        String outPath = "./out.xlsx";
        List<EntityAdderTemp> results = new ArrayList<>();
        ExcelWriter excelWriter = EasyExcel.write(outPath, EntityAdderTemp.class).build();
        ExcelReaderBuilder excelReaderBuilder = EasyExcel.read(path);
        ExcelReader excelReader = excelReaderBuilder.build();
        List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
        for(ReadSheet sheet : sheets) {
            results.clear();
            List<TestOutEntity> list = EasyExcel.read(path).head(TestOutEntity.class).sheet(sheet.getSheetName()).doReadSync();
            WriteSheet writeSheet = EasyExcel.writerSheet(sheet.getSheetName()).build();
            for(TestOutEntity entityOut : list) {
                String id = entityOut.getId();
                String question = entityOut.getContent();
                String answer = entityOut.getAnswer();
                String source = entityOut.getSource();
                String pred = entityOut.getBestPred();
                if(!entityOut.getIfCorrect().equals("无预测结果")) {
                    continue;
                }
                List<String> predEnList = new ArrayList<>();
                if(!StringUtils.isEmpty(answer) && !StringUtils.isEmpty(source) && !StringUtils.isEmpty(pred)) {
                    List<Entity> entityListSource = neoManager.getEntityListFromName(source);
                    if(entityListSource.size() == 0) {
                        continue;
                    }
                    try{
                        predEnList = RuleHandler.geAlltPropertyAbbrWithoutSubject(pred);
                    }
                    catch (Exception e) {

                    }
                    if(StringUtils.isEmpty(pred)) {
                        continue;
                    }
                    if(CollectionUtils.isEmpty(predEnList)) {
                        predEnList.add(pred);
                    }
                    Entity entitySource = entityListSource.get(0);
                    for(String predEn : predEnList) {
                        if(predEn.contains("-P")) {
                            results.add(EntityAdderTemp.builder()
                                    .id(id)
                                    .question(question)
                                    .source(source)
                                    .sourceUri(entitySource.getUri())
                                    .predicate(predEn)
                                    .predicateChinese(pred)
                                    .object(answer)
                                    .type("属性")
                                    .build());
                        }
                        else if(predEn.contains("-R")) {
                            List<Entity> entityListTarget = neoManager.getEntityListFromName(answer);
                            if(entityListTarget.size() > 0) {
                                Entity entityTarget = entityListTarget.get(0);
                                results.add(EntityAdderTemp.builder()
                                        .id(id)
                                        .question(question)
                                        .source(source)
                                        .sourceUri(entitySource.getUri())
                                        .predicate(predEn)
                                        .predicateChinese(pred)
                                        .object(answer)
                                        .objectUri(entityTarget.getUri())
                                        .type("关系")
                                        .build());
                            }
                        }
                        else {
                            continue;
                        }
                    }
                }
            }
            excelWriter.write(results, writeSheet);
        }
        excelWriter.finish();
    }

    /**
     * 根据分析好的谓词分析取补充图谱
     */
    @Test
    public void completGraph() {
        String path = "./out.xlsx";
        ExcelReaderBuilder excelReaderBuilder = EasyExcel.read(path);
        ExcelReader excelReader = excelReaderBuilder.build();
        List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
        for(ReadSheet sheet : sheets) {
            List<EntityAdderTemp> list = EasyExcel.read(path).head(EntityAdderTemp.class).sheet(sheet.getSheetName()).doReadSync();
            for(EntityAdderTemp entityOut : list) {

                if(entityOut.getType().equals("属性")) {
                    Property property = Property.builder()
                            .subject(entityOut.getSource())
                            .predicate(entityOut.getPredicate())
                            .object(entityOut.getObject())
                            .build();
                    String uri = entityOut.getSourceUri();
//                    neoManager.updateProperty(uri, null, property);
                    System.out.println(JSONArray.toJSONString(property));
                }
                else if(entityOut.getType().equals("关系")) {
                    Relation relation = Relation.builder()
                            .subject(entityOut.getSource())
                            .subjectUri(entityOut.getSourceUri())
                            .predicate(entityOut.getPredicate())
                            .object(entityOut.getObject())
                            .objectUri(entityOut.getObjectUri())
                            .build();
//                    neoManager.updateRelation(null, relation);
                    System.out.println(JSONArray.toJSONString(relation));
                }
                else {
                    continue;
                }
            }
        }
    }

    /**
     * 补充作者作品关系
     * @throws IOException
     */
    @Test
    public void addAuthorPoet() throws IOException {
        String path = "./authorPoetOut.txt";
        File file =new File(path);
        List<String> contents = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File deleteUriRecord = new File("./deleteUriRecord.txt");
        FileWriter fileWritter = new FileWriter(deleteUriRecord.getName(),true);
        StringBuilder sb = new StringBuilder();
        File adder = new File("./needAdderRecord.txt");
        FileWriter fileWritterAdder = new FileWriter(adder.getName(),true);
        StringBuilder sbAdder = new StringBuilder();
        File adderJson = new File("./needAdderJson.txt");
        FileWriter fileWritterAdderJson = new FileWriter(adderJson.getName(),true);
        StringBuilder sbAdderJson = new StringBuilder();
        for(String content : contents) {
            String author = content.split(" ")[0];
            String poet = content.split(" ")[1];
            List<String> books = CommonUtil.getMiddleTextFromTags(poet, "《", "》");
            if(books.size() > 1) {
                List<Entity> judge = neoManager.getEntityListFromName(poet);
                if(judge.size() > 0) {
                    for(Entity entity : judge) {
                        sb.append(entity.getName() + " " + entity.getUri() + "\n");
                    }
                }
            }
            for(String book : books) {
                List<Entity> entityListAu = neoManager.getEntityListFromName(author);
                List<Entity> entityListPo = neoManager.getEntityListFromName(book);
                if(!CollectionUtils.isEmpty(entityListAu) && !CollectionUtils.isEmpty(entityListPo)) {
                    Entity entityAu = neoManager.getEntityFromUri(entityListAu.get(0).getUri());
                    Entity entityPo = neoManager.getEntityFromUri(entityListPo.get(0).getUri());
                    Relation next1 = Relation.builder()
                            .subject(book)
                            .subjectUri(entityPo.getUri())
                            .object(author)
                            .objectUri(entityAu.getUri())
                            .predicate("edukg_prop_chinese__main-R2")
                            .build();
                    Relation next2 = Relation.builder()
                            .subject(author)
                            .subjectUri(entityAu.getUri())
                            .object(book)
                            .objectUri(entityPo.getUri())
                            .predicate("edukg_prop_chinese__main-R1")
                            .build();
                    boolean needNext2 = true;
                    for(Relation relation : entityAu.getRelation() == null ? new ArrayList<Relation>() : entityAu.getRelation()) {
                        if(relation.getSubject().equals(author) &&
                                relation.getObject().equals(book) &&
                                relation.getPredicate().equals("edukg_prop_chinese__main-R1")) {
                            needNext2 = false;
                        }
                    }
                    boolean needNext1 = true;
                    for(Relation relation : entityPo.getRelation() == null ? new ArrayList<Relation>() : entityPo.getRelation()) {
                        if(relation.getSubject().equals(book) &&
                                relation.getObject().equals(author) &&
                                relation.getPredicate().equals("edukg_prop_chinese__main-R2")) {
                            needNext1 = false;
                        }
                    }
                    if(needNext1) {
                        sbAdderJson.append(JSON.toJSONString(next1) + "\n");
                    }
                    if(needNext2) {
                        sbAdderJson.append(JSON.toJSONString(next2) + "\n");
                    }
//                neoManager.updateRelation(null, next);
                }
                if(CollectionUtils.isEmpty(entityListAu)) {
                    sbAdder.append(author + "\n");
                }
                if(CollectionUtils.isEmpty(entityListPo)) {
                    sbAdder.append(book + "\n");
                }
            }
            fileWritter.write(sb.toString());
            fileWritter.flush();
            sb.delete(0, sb.length());
            fileWritterAdder.write(sbAdder.toString());
            fileWritterAdder.flush();
            sbAdder.delete(0, sbAdder.length());
            fileWritterAdderJson.write(sbAdderJson.toString());
            fileWritterAdderJson.flush();
            sbAdderJson.delete(0, sbAdderJson.length());
        }
        fileWritter.close();
        fileWritterAdder.close();
    }

    @Test
    public void addStory() throws IOException {
        String path = "./storyOut.txt";
        File file =new File(path);
        List<String> contents = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File adderJson = new File("./needAdderJson.txt");
        FileWriter fileWritterAdderJson = new FileWriter(adderJson.getName(),false);
        StringBuilder sbAdderJson = new StringBuilder();
        List<Entity> entitys = new ArrayList<>();
        String label = "edukg_cls_chinese__main-C16";
        String subject = "chinese";
        for(String content : contents) {
            if(StringUtils.isEmpty(content)) {
                continue;
            }
            content = content.replace("【", "");
            content = content.replace("】", "");
            List<String> vanish = CommonUtil.getMiddleTextFromTags(content, "（", "）");
            for(String s : vanish) {
                content = content.replace(s, "");
            }
            String tag = "";
            content = content.trim();
            if(content.contains("　")) {
                tag = "　";
            }
            else if(content.contains("：")) {
                tag = "：";
            }
            String title = content.split(tag)[0];
            String value = content.split(tag)[1];
            List<Entity> entityList = neoManager.getEntityListFromName(title);
            if(!CollectionUtils.isEmpty(entityList)) {
                continue;
            }
            List<Property> properties = new ArrayList<>();
            properties.add(Property.builder()
                    .subject(title)
                    .predicate("edukg_prop_common__main-P4")
                    .object(value)
                    .build());
            entitys.add(Entity.builder()
                    .name(title)
                    .property(properties)
                    .build());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("subject", subject);
        jsonObject.put("label", label);
        jsonObject.put("entities", entitys);
        fileWritterAdderJson.write(jsonObject.toJSONString());
        fileWritterAdderJson.flush();
        fileWritterAdderJson.close();
    }
}
