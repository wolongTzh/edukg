package com.tsinghua.edukg.utils;

import com.tsinghua.edukg.constant.WebConstant;
import com.tsinghua.edukg.model.WebResInfo;

/**
 * controller返回结果构建工具类
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public class WebUtil {

    public static WebResInfo successResult(Object data) {
        WebResInfo webResInfo = new WebResInfo();
        webResInfo.setCode(WebConstant.RESULT_OK_CODE);
        webResInfo.setData(data);
        webResInfo.setMessage(WebConstant.RESULT_OK);
        return webResInfo;
    }

    public static WebResInfo errResult(Integer code, String msg) {
        WebResInfo webResInfo = new WebResInfo();
        webResInfo.setCode(code);
        webResInfo.setData(null);
        webResInfo.setMessage(msg);
        return webResInfo;
    }
}
