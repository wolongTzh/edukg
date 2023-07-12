package com.tsinghua.edukg.controller;

import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.service.ToolService;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "api/tool")
@Slf4j
public class ToolServiceController {

    @Autowired
    ToolService toolService;


    /**
     * 查找谓词抽取用到的example
     *
     * @return
     */
    @GetMapping(value = "getExample")
    public WebResInfo getExample(String predicate, int type) throws IOException {
        List<JSONObject> retList = toolService.getExamples(predicate, type);
        return WebUtil.successResult(retList);
    }
}
