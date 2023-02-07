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
public class StatisticResult {

    @ExcelProperty(value = "对应测试集名称")
    String name;

    @ExcelProperty(value = "样例总数")
    int totalCount;

    @ExcelProperty(value = "正确样例数")
    int correctCount;

    @ExcelProperty(value = "正确样例占比")
    double correctPercent;
}
