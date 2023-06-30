package com.tsinghua.edukg;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONArray;
import com.tsinghua.edukg.api.feign.QAFeignService;
import com.tsinghua.edukg.api.model.ApiResult;
import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.api.model.QAResult;
import com.tsinghua.edukg.model.excel.StatisticResult;
import com.tsinghua.edukg.model.excel.TestOutEntity;
import com.tsinghua.edukg.model.excel.TestSourceEntity;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.RuleHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
public class QATest {

    String path = "C:\\Users\\feifei\\Downloads\\测试集\\测试集-添加来源\\添加信息来源\\地理测试题1000（已添加来源）.xlsx";
    String outPath = "C:\\Users\\feifei\\Downloads\\测试集\\测试集-添加来源\\添加信息来源\\output.xlsx";
    String outPath2 = "C:\\Users\\feifei\\Downloads\\测试集\\测试集-添加来源\\添加信息来源\\statistic.xlsx";
    String basePath = "C:\\Users\\feifei\\Downloads\\测试集\\测试集-添加来源\\添加信息来源";

    @Resource
    QAFeignService qaFeignService;

    @Test
    public void testQA() throws IllegalAccessException {
        String question = "我国的首都在哪里";
        QAParam qaParam = new QAParam();
        qaParam.setQuestion(question);
        ApiResult<QAResult> qaResult = qaFeignService.qaRequest(CommonUtil.entityToMutiMap(qaParam));
        QAResult accQaResult = qaResult.getData();
        log.info(JSONArray.toJSONString(accQaResult));
    }
    @Test
    public void readExcel() throws IOException {
        List<TestSourceEntity> list = EasyExcel.read(path).head(TestSourceEntity.class).sheet().doReadSync();
        System.out.println("数据体：" + JSONArray.toJSONString(list));
    }

    @Test
    public void outExcel() {
        List<TestOutEntity> list = new ArrayList<>();
        ExcelWriter excelWriter = EasyExcel.write(outPath, TestOutEntity.class).build();
        // 表单循环
        for(int j=0; j<3; j++) {
            // 数据循环
            for(int i=0; i<10; i++) {
                list.add(TestOutEntity.builder()
                        .id((i+1) + "")
                        .answer("原答案")
                        .answerByAlgo("算法给出答案")
                        .ifCorrect("正确")
                        .build());
            }
            WriteSheet writeSheet = EasyExcel.writerSheet("表单" + (j+1)).build();
            excelWriter.write(list, writeSheet);
        }
        excelWriter.finish();
    }

    @Test
    public void handleAllFiles() throws IllegalAccessException {
        File file1 = new File(basePath);
        //判断是否有目录
        if(file1.isDirectory()) {
            //获取目录中的所有文件名称
            String[] names = file1.list();
            ExcelWriter excelWriter = EasyExcel.write(outPath, TestOutEntity.class).build();
            ExcelWriter excelWriter2 = EasyExcel.write(outPath2, StatisticResult.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("统计结果").build();
            List<StatisticResult> results = new ArrayList<>();
            for(String str : names) {
                String path = basePath + "\\" + str;
                results.add(handleOneFile(excelWriter, path, str));
            }
            excelWriter2.write(results, writeSheet);
            excelWriter.finish();
            excelWriter2.finish();
        }
    }

    public StatisticResult handleOneFile(ExcelWriter writer, String path, String name) throws IllegalAccessException {
        List<TestSourceEntity> list = EasyExcel.read(path).head(TestSourceEntity.class).sheet().doReadSync();
        int correctCount = 0;
        WriteSheet writeSheet = EasyExcel.writerSheet(name).build();
        List<TestOutEntity> outList = new ArrayList<>();
        for(TestSourceEntity entity : list) {
            TestOutEntity outEntity = getAndjudgeAnswer(entity, name);
            if(outEntity == null) {
                continue;
            }
            if(outEntity.getIfCorrect().equals("正确")) {
                correctCount++;
            }
            outList.add(outEntity);
        }
        writer.write(outList, writeSheet);
        return StatisticResult.builder()
                .name(name)
                .correctCount(correctCount)
                .totalCount(list.size())
                .correctPercent(correctCount * 1.0 / list.size())
                .build();
    }

    public TestOutEntity getAndjudgeAnswer(TestSourceEntity source, String name) throws IllegalAccessException {
        String question = source.getContent();
        String answer = source.getAnswer();
        QAParam qaParam = new QAParam();
        qaParam.setQuestion(source.getContent());
        log.info("正在处理测试集：" + name + "-----问题编号：" + source.getContent() + "-----问题：" + source.getContent());
        ApiResult<QAResult> qaResult = new ApiResult<>();
        try {
            qaResult = qaFeignService.qaRequest(CommonUtil.entityToMutiMap(qaParam));
        }
        catch (Exception e) {
            return null;
        }
        QAResult accQaResult = qaResult.getData();
        String algoAnswer = "";
        String sourceEntityName = "";
        String predicate = "";
        String targetEntityName = "";
        String template = "";
        if(accQaResult != null) {
            algoAnswer = accQaResult.getAnswerValue() == null ? "" : accQaResult.getAnswerValue();
            sourceEntityName = accQaResult.getSubject();
          //  accQaResult.setPredicate(RuleHandler.getPropertyNameByAbbr(accQaResult.getPredicate() == null ? "" : accQaResult.getPredicate()));
            predicate = accQaResult.getPredicate() == null ? "" : accQaResult.getPredicate();
            targetEntityName = accQaResult.getObject() == null ? "" : accQaResult.getObject();
            template = accQaResult.getTemplateContent() == null ? "" : accQaResult.getTemplateContent();
        }
        TestOutEntity output = TestOutEntity.builder()
                .id(source.getId())
                .content(question)
                .answer(answer)
                .answerByAlgo(algoAnswer)
                .source(sourceEntityName)
                .predicate(predicate)
                .target(targetEntityName)
                .template(template)
                .ifCorrect(judgeCorrect(answer, algoAnswer, name, accQaResult, question))
                .build();
        return output;
    }

    public boolean chemistryCompare(String answer, String answerOfAlgo) {
        answer = answer.replaceAll("\\{|}|_|\\^|-|\\+| ", "").replace("$","");
        answerOfAlgo = answerOfAlgo.replaceAll("\\{|}|_|\\^|-|\\+| ", "").replace("$", "");
        return answer.contains(answerOfAlgo) || answerOfAlgo.contains(answer);
    }

    public String judgeCorrect(String answer, String answerOfAlgo, String name, QAResult qaResult, String question) {
        if(StringUtils.isEmpty(answer)) {
            return "无答案";
        }
        if(StringUtils.isEmpty(answerOfAlgo)) {
            return "无预测结果";
        }
        boolean ifCorrect = false;
        if(name.equals("化学测试题1000（已添加来源）.xlsx")) {
            ifCorrect = chemistryCompare(answer, answerOfAlgo);
        }
        else if(answer.contains(answerOfAlgo) || answerOfAlgo.contains(answer)) {
            ifCorrect = true;
        }
        if(!ifCorrect) {
            if(question.contains(qaResult.getSubject()) && question.contains(qaResult.getPredicate())) {
                return "目标实体信息错误或误杀";
            }
            if(!question.contains(qaResult.getSubject())) {
                return "需要人工判断";
            }
            if(!question.contains(qaResult.getSubject()) && !question.contains(qaResult.getPredicate())) {
                return "模板错误";
            }
         }
        return "正确";
    }

    @Test
    public void innerTest() {
        String answer = "Mg{ 2}";
        String answerOfAlgo = "$Mg^{2+}$";
        chemistryCompare(answer, answerOfAlgo);
    }
}
