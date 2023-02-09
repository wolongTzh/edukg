package com.tsinghua.edukg.model;

import lombok.Builder;
import lombok.Data;

/**
 * 实体属性
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Data
@Builder
public class Property {

    String subject;

    String predicate;

    String predicateLabel;

    String object;
}
