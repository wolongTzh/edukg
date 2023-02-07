package com.tsinghua.edukg.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 批量增加实体VO参数
 *
 * @author tanzheng
 * @date 2022/10/12
 */

@Data
@AllArgsConstructor
public class QuikAddEntitiesVO {

    String name;

    String uri;
}
