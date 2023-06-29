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
public class NeedCompletion {

    @ExcelProperty(value = "主语")
    String subject;

    @ExcelProperty(value = "uri")
    String uri;

    @ExcelProperty(value = "谓词")
    String predicate;

    @ExcelProperty(value = "宾语")
    String object;
}
