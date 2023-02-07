package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 各数据来源实体数统计
 *
 * @author tanzheng
 * @date 2022/10/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceAndCount {

    String source;

    Integer count;
}
