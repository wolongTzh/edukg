package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体属性
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Property {

    String subject;

    String predicate;

    String predicateLabel;

    String object;
}
