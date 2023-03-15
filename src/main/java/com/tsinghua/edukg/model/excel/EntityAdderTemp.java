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
public class EntityAdderTemp {

    @ExcelProperty(value = "编号")
    String id;

    @ExcelProperty(value = "问题")
    String question;

    @ExcelProperty(value = "源实体")
    String source;

    @ExcelProperty(value = "源实体Uri")
    String sourceUri;

    @ExcelProperty(value = "谓词")
    String predicate;

    @ExcelProperty(value = "谓词解释")
    String predicateChinese;

    @ExcelProperty(value = "目标值")
    String object;

    @ExcelProperty(value = "目标实体Uri(如果是实体)")
    String objectUri;

    @ExcelProperty(value = "属性or关系")
    String type;
}
