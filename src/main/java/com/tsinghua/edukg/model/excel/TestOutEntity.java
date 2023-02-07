package com.tsinghua.edukg.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOutEntity {

    @ExcelProperty(value = "编号")
    String id;

    @ExcelProperty(value = "题目")
    String content;

    @ExcelProperty(value = "答案")
    String answer;

    @ExcelProperty(value = "算法给出答案")
    String answerByAlgo;

    @ExcelProperty(value = "源实体")
    String source;

    @ExcelProperty(value = "谓词")
    String predicate;

    @ExcelProperty(value = "目标实体")
    String target;

    @ExcelProperty(value = "算法预测模板")
    String template;

    @ExcelProperty(value = "答案是否正确")
    String ifCorrect;
}
