package com.tsinghua.edukg.model.params;

import lombok.Data;

/**
 * getEntityList接口参数
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Data
public class GetEntityListParam {

    String className;

    Integer pageNo;

    Integer pageSize;

    String keyWord;

    String subject;
}
