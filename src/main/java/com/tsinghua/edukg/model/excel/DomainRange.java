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
public class DomainRange {

    @ExcelProperty(value = "谓词信息")
    String pred;
    @ExcelProperty(value = "domain")
    String domain;
    @ExcelProperty(value = "range")
    String range;
    @ExcelProperty(value = "举例")
    String example;
    @ExcelProperty(value = "属性or关系")
    String predOrRelation;
}
