package com.tsinghua.edukg.api.model;

import lombok.Data;

/**
 * 接口返回结果
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Data
public class ApiResult<T> {

    int code;

    String msg;

    T data;
}
