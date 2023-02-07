package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 图谱实体
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class Entity extends EntitySimp {

    List<Property> property;

    List<Relation> relation;
}
