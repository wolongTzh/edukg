package com.tsinghua.edukg.tools;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@SpringBootTest
@Slf4j
public class LinkingEntitiesCombination {

    Map<String, String> linkingPathMap = new HashMap<String, String>(){{
        put("biology", "static/processed_3.0/biology_concept_entities.csv");
        put("chemistry", "static/processed_3.0/chemistry_concept_entities.csv");
        put("chinese", "static/processed_3.0/chinese_concept_entities.csv");
        put("geo", "static/processed_3.0/geo_concept_entities.csv");
        put("history", "static/processed_3.0/history_concept_entities.csv");
        put("math", "static/processed_3.0/math_concept_entities.csv");
        put("physics", "static/processed_3.0/physics_concept_entities.csv");
        put("politics", "static/processed_3.0/politics_concept_entities.csv");
    }};

    @Test
    public void combineLinking() throws IOException {
        // 组装set: 需要处理的实体名与需要保留的uri
        Set<String> needHandleName = new HashSet<>();
        Set<String> remainUris = new HashSet<>();
        JSONArray nameUriArray = CommonUtil.readJsonOut("./name2uri.json").getJSONArray("content");
        for(int i=0; i<nameUriArray.size(); i++) {
            JSONObject nameUri = nameUriArray.getJSONObject(i);
            String name = (String) nameUri.get("name");
            String remainUri = (String) nameUri.get("result");
            needHandleName.add(name);
            remainUris.add(remainUri);
        }
        // 实体链接实体去重合并，转储到新文件中
        File conceptEntities = new File("./concept_entities.csv");
        FileWriter fileWritter = new FileWriter(conceptEntities.getName(),true);
        StringBuilder sb = new StringBuilder();
        sb.append("uri,cls,label\n");
        for(Map.Entry entry : linkingPathMap.entrySet()) {
            String path = (String) entry.getValue();
            List<String> contentList = CommonUtil.readTextInResource(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path)));
            boolean startTag = true;
            int uri = 0;
            int label = 0;
            int cls = 0;
            for(String content : contentList) {
                String[] spliter = content.split(",");
                if(startTag) {
                    startTag = false;
                    int count = -1;
                    for(String name : spliter) {
                        count++;
                        if(name.equals("uri")) {
                            uri = count;
                        }
                        if(name.equals("label")) {
                            label = count;
                        }
                        if(name.equals("cls")) {
                            cls = count;
                        }
                    }
                    continue;
                }
                if(spliter[uri].startsWith("<")) {
                    spliter[uri] = spliter[uri].substring(1);
                }
                if(spliter[uri].endsWith(">")) {
                    spliter[uri] = spliter[uri].substring(0, spliter[uri].length() - 1);
                }
                String name = spliter[label];
                String uriEn = spliter[uri];
                String clsEn = spliter[cls];
                if(needHandleName.contains(name) && !remainUris.contains(uriEn)) {
                    continue;
                }
                sb.append(uriEn + "," + clsEn + "," + name + "\n");
            }
        }
        fileWritter.write(sb.toString());
        fileWritter.flush();
        fileWritter.close();
    }
}
