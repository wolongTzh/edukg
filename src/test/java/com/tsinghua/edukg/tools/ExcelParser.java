package com.tsinghua.edukg.tools;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.api.feign.QAFeignService;
import com.tsinghua.edukg.api.model.ApiResult;
import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.api.model.QAResult;
import com.tsinghua.edukg.model.excel.StatisticResult;
import com.tsinghua.edukg.model.excel.TestOutEntity;
import com.tsinghua.edukg.model.excel.TestSourceEntity;
import com.tsinghua.edukg.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@Slf4j
public class ExcelParser {

    @Test
    public void genKgMissInfo() throws IOException {
        String path = "./output1.xlsx";
        String annoPath = "./parse_anno.json";
        String outPath = "./out5.xlsx";
        JSONArray jsonArray = CommonUtil.readJsonArray(annoPath);
        List<TestOutEntity> list = EasyExcel.read(path).head(TestOutEntity.class).sheet(null, "chinese").doReadSync();
        ExcelWriter excelWriter = EasyExcel.write(outPath, OutEntity.class).build();
        List<OutEntity> outEntities = new ArrayList<>();
        for(TestOutEntity out : list) {
            if(out.getIfCorrect().equals("答案错误")) {
                JSONObject jsonObject = jsonArray.getJSONObject(Integer.parseInt(out.getId()) - 1);
                if(!jsonObject.getString("predicate").equals(out.getBestPred()) || !jsonObject.getString("subject").equals(out.getSource())) {
                    continue;
                }
                outEntities.add(OutEntity.builder()
                                .subject(out.getSource())
                                .predicate(out.getPredicate())
                                .predicateAnswer(out.getAnswerByAlgo())
                                .answer(out.getAnswer())
                                .question(out.getContent())
                                .id(out.getId())
                        .build());
            }
        }
        WriteSheet writeSheet = EasyExcel.writerSheet("统计结果").build();
        excelWriter.write(outEntities, writeSheet);
        excelWriter.finish();
    }

    @Test
    public void compareOutDiffer() throws IOException {
        String path1 = "./output.xlsx";
        String path2 = "./output1.xlsx";
        String outPath1 = "./out1.xlsx";
        String outPath2 = "./out2.xlsx";
        String outPath3 = "./out3.xlsx";
        ExcelWriter excelWriter1 = EasyExcel.write(outPath1, TestOutEntity.class).build();
        ExcelWriter excelWriter2 = EasyExcel.write(outPath2, TestOutEntity.class).build();
        ExcelWriter excelWriter3 = EasyExcel.write(outPath3, TestOutEntity.class).build();
        List<String> sheetNames = Arrays.asList("chinese");
        for(String sheetName : sheetNames) {
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
            List<TestOutEntity> outEntities1 = new ArrayList<>();
            List<TestOutEntity> outEntities2 = new ArrayList<>();
            List<TestOutEntity> outEntities3 = new ArrayList<>();
            List<TestOutEntity> list1 = EasyExcel.read(path1).head(TestOutEntity.class).sheet(null, sheetName).doReadSync();
            List<TestOutEntity> list2 = EasyExcel.read(path2).head(TestOutEntity.class).sheet(null, sheetName).doReadSync();
            for(int i=0; i<list1.size(); i++) {
                TestOutEntity entity1 = list1.get(i);
                TestOutEntity entity2 = list2.get(i);
                if(!Objects.equals(entity1.getAnswerByAlgo(), entity2.getAnswerByAlgo()) && !entity1.getIfCorrect().equals("正确") && entity2.getIfCorrect().equals("正确")) {
                    outEntities1.add(entity1);
                    outEntities1.add(entity2);
                    outEntities1.add(TestOutEntity.builder().build());
                }
            }
            excelWriter1.write(outEntities1, writeSheet);
            for(int i=0; i<list1.size(); i++) {
                TestOutEntity entity1 = list1.get(i);
                TestOutEntity entity2 = list2.get(i);
                if(!Objects.equals(entity1.getAnswerByAlgo(), entity2.getAnswerByAlgo()) && entity1.getIfCorrect().equals("正确") && !entity2.getIfCorrect().equals("正确")) {
                    outEntities2.add(entity1);
                    outEntities2.add(entity2);
                    outEntities2.add(TestOutEntity.builder().build());
                }
            }
            for(int i=0; i<list1.size(); i++) {
                TestOutEntity entity1 = list1.get(i);
                TestOutEntity entity2 = list2.get(i);
                if(!Objects.equals(entity1.getAnswerByAlgo(), entity2.getAnswerByAlgo()) && !entity1.getIfCorrect().equals("正确") && !entity2.getIfCorrect().equals("正确")) {
                    outEntities3.add(entity1);
                    outEntities3.add(entity2);
                    outEntities3.add(TestOutEntity.builder().build());
                }
            }
            excelWriter1.write(outEntities1, writeSheet);
            excelWriter2.write(outEntities2, writeSheet);
            excelWriter3.write(outEntities3, writeSheet);
        }
        excelWriter1.finish();
        excelWriter2.finish();
        excelWriter3.finish();
    }
}
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class OutEntity {

    public String id;

    public String question;

    public String subject;

    public String predicate;
    public String predicateAnswer;

    public String answer;
}
