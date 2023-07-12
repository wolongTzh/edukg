package com.tsinghua.edukg.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.service.ToolService;
import com.tsinghua.edukg.utils.HtmlParserUtil;
import com.tsinghua.edukg.utils.SparqlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
public class ToolServiceImpl implements ToolService {

    @Autowired
    HtmlParserUtil htmlParserUtil;

    @Override
    public List<JSONObject> getExamples(String predicate, int type) throws IOException {
        List<JSONObject> jsonObjectList = new ArrayList<>();
        List<String> retList = SparqlUtil.mainProcess(predicate, type);
        for(String ret : retList) {
            System.out.println(ret);
            JSONObject jsonObject = htmlParserUtil.mainProcess(ret, predicate, type);
            jsonObjectList.add(jsonObject);
        }
        return jsonObjectList;
    }
}
