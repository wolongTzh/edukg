package com.tsinghua.edukg.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSourceEntity {

    @ExcelProperty(value = "编号")
    String id;

    @ExcelProperty(value = "题目")
    String content;

    @ExcelProperty(value = "答案")
    String answer;

    @ExcelProperty(value = "来源")
    String source;
}
