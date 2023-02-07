package com.tsinghua.edukg.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计图谱数据来源VO参数
 *
 * @author tanzheng
 * @date 2022/10/12
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTotalStatusVO {

    Integer classes;

    Integer classesGt50;

    Integer entities;

    Integer relations;

    Integer properties;
}
