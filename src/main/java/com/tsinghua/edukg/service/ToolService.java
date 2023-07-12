package com.tsinghua.edukg.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ToolService {

    public List<JSONObject> getExamples(String predicate, int type) throws IOException;
}
