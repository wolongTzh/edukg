package com.tsinghua.edukg.model;

import lombok.Data;

@Data
public class GetEntityListParam {

    String className;

    Integer pageNo;

    Integer pageSize;

    String keyWord;

    String subject;
}
