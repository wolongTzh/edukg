package com.tsinghua.edukg.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RuleHandler {

    @Autowired
    ResourceLoader resourceLoader;

    String subjectLabelTemplate = "edukg_cls_%s__main-C0";

    Map<String, String> subjectZh2En = new HashMap<String, String>(){{
        put("语文", "chinese");
        put("数学", "math");
        put("英语", "english");
        put("物理", "physics");
        put("化学", "chemistry");
        put("生物", "biology");
        put("历史", "history");
        put("地理", "geo");
        put("政治", "politics");
    }};

    String cls2labelPath = new ClassPathResource("/static/cls2label.json").getURI().getPath();

    String pred2labelPath = new ClassPathResource("/static/pred2label.json").getURI().getPath();

    String prefixesPath = new ClassPathResource("/static/prefixes.json").getURI().getPath();

    String subClassOfPath = new ClassPathResource("/static/subClassOf.json").getURI().getPath();

    public Map<String, String> cls2labelMap;

    public Map<String, String> pred2labelMap;

    public Map<String, String> prefixesMap;

    public Map<String, List<String>> subClassOfMap;


    @Autowired
    public RuleHandler() throws IOException {
        cls2labelMap = readJson(cls2labelPath);
        pred2labelMap = readJson(pred2labelPath);
        prefixesMap = readJson(prefixesPath);
        subClassOfMap = readJson(subClassOfPath);
    }

    public String convertSubjectZh2En(String subject) {
        return subjectZh2En.get(subject);
    }

    public String convertSubject2Label(String subject) {
        return String.format(subjectLabelTemplate, subject);
    }

    public Map readJson(String jsonPath) throws IOException {
        File file = new File(jsonPath);
        Reader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
        BufferedReader br = new BufferedReader(reader);
        StringBuffer sb = new StringBuffer();
        String s = null;
        while((s = br.readLine()) != null){
            sb.append(s);
        }
        reader.close();
        Map map = JSON.parseObject(sb.toString());
        return map;
    }
}
