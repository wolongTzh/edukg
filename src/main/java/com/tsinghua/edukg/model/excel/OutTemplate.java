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
public class OutTemplate {

    @ExcelProperty(value = "id")
    String id;

    @ExcelProperty(value = "question")
    String question;

    @ExcelProperty(value = "answer")
    String answer;

    @ExcelProperty(value = "subject")
    String subject;

    @ExcelProperty(value = "predicateAnswer")
    String predicateAnswer;

    @ExcelProperty(value = "type")
    String type;

    @ExcelProperty(value = "predicate")
    String predicate;

    @ExcelProperty(value = "label")
    String label;
}
