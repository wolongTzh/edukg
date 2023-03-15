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
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.excel.EntityAdderTemp;
import com.tsinghua.edukg.model.excel.TestOutEntity;
import com.tsinghua.edukg.model.excel.TestSourceEntity;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.RuleHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        String path = "./output-kb-0310(1).xlsx";
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
                String pred = entityOut.getPredicate();
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
}
