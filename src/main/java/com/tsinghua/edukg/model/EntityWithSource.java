package com.tsinghua.edukg.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 图谱来源+实体信息
 *
 * @author tanzheng
 * @date 2023/04/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class EntityWithSource extends Entity {

    Source source;
}
