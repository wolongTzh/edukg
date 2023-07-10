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
public class Cls2Entity {

    @ExcelProperty(value = "cls信息")
    String cls;

    @ExcelProperty(value = "实体名")
    String entityName;

    @ExcelProperty(value = "实体class信息")
    String entityClass;

    @ExcelProperty(value = "实体uri")
    String entityUri;
}
