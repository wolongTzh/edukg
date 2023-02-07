package com.tsinghua.edukg.model;

import lombok.Data;

/**
 * 接口返回结果
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Data
public class WebResInfo {

    int code;

    String message;

    Object data;
}
