package com.tsinghua.edukg.model.params;

import lombok.Data;

/**
 * getAllEntityList接口参数
 *
 * @author tanzheng
 * @date 2022/10/13
 */
@Data
public class GetAllEntityListParam {

    String className;

    String keyWord;

    String subject;
}
