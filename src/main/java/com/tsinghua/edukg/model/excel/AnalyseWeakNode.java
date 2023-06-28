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
public class AnalyseWeakNode {

    @ExcelProperty(value = "实体名称")
    String name;

    @ExcelProperty(value = "uri")
    String uri;

    @ExcelProperty(value = "实体详细信息")
    String content;

    @ExcelProperty(value = "实体所属类型")
    String type;
}
