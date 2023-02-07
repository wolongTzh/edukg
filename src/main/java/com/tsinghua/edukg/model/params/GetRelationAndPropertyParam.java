package com.tsinghua.edukg.model.params;

import lombok.Data;

/**
 * 获取实体关系和属性接口参数
 *
 * @author tanzheng
 * @date 2022/10/13
 */
@Data
public class GetRelationAndPropertyParam {

    String keyword;

    String subject;
}
