package com.tsinghua.edukg.service;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.config.RedisConfig;
import com.tsinghua.edukg.constant.BusinessConstant;
import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.manager.NeoAssisManager;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.manager.RedisManager;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.VO.LinkingVO;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.params.HotEntitiesParam;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.model.params.SearchSubgraphParam;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.JiebaHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * graph service impl
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Service
public class GraphServiceImpl implements GraphService {

    @Resource
    NeoManager neoManager;

    @Resource
    RedisManager redisManager;

    @Resource
    NeoAssisManager neoAssisManager;

    @Autowired
    private JiebaHelper segmenter;

    private Integer openGate;

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

    Map<String, List<String>> linkingContentMap = new HashMap<>();

    String splitTag = "@splitTag@";

    @Autowired
    public GraphServiceImpl(RedisConfig redisConfig) throws IOException {
        openGate = redisConfig.getOpenGate();
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
                }
                if(spliter[uri].startsWith("<")) {
                    spliter[uri] = spliter[uri].substring(1);
                }
                if(spliter[uri].endsWith(">")) {
                    spliter[uri] = spliter[uri].substring(0, spliter[uri].length() - 1);
                }
                List<String> back = linkingContentMap.getOrDefault(spliter[label], new ArrayList<>());
                String add = spliter[uri] + splitTag + spliter[cls];
                back.add(add);
                linkingContentMap.put(spliter[label], back);
            }
        }
     }

    @Override
    public Entity getEntityFromUri(String uri) {
        Entity entity = neoManager.getEntityFromUri(uri);
        if(entity.getUri() == null) {
            return null;
        }
        return entity;
    }

    @Override
    public List<Entity> getEntityFromName(String searchText) {
        List<Entity> entityList = neoManager.getEntityListFromName(searchText);
        return entityList;
    }

    @Override
    public List<Entity> getHotEntities(HotEntitiesParam param) {
        if(openGate == 0) {
            return neoAssisManager.getHotEntities(param.getSubject());
        }
        return redisManager.getHotEntities(param.getSubject());
    }

    @Override
    public List<Relation> searchSubgraph(SearchSubgraphParam param) {
        List<String> instanceList = param.getInstanceList();
        List<Relation> relations = neoManager.findPathBetweenNodes(instanceList.get(0), instanceList.get(1), BusinessConstant.MAX_JUMP_TIME);
        if(relations == null || relations.size() == 0) {
            throw new BusinessException(BusinessExceptionEnum.START_OR_TAIL_URI_ERROR);
        }
        return relations;
    }

    @Override
    public List<LinkingVO> linkingEntities(LinkingParam param) {
        String text = param.getSearchText();
        List<String> segResult = segmenter.cutWords(text);
        List<LinkingVO> result = new ArrayList<>();
        Map<String, List<String>> sourceMap = linkingContentMap;
        if(sourceMap == null) {
            return null;
        }
        int start = 0;
        int end = 0;
        Map<String, LinkingVO> linkingVOMap = new HashMap<>();
        for(String seg : segResult) {
            start = end;
            end += seg.length();
            if(sourceMap.containsKey(seg)) {
                List<String> outContent = sourceMap.get(seg);
                for(String out : outContent) {
                    List<String> content = Arrays.asList(out.split(splitTag));
                    List<Integer> whereSingle = Arrays.asList(start, end);
                    LinkingVO linkingVO = linkingVOMap.getOrDefault(seg+content.get(0), new LinkingVO(seg, content.get(0), new ArrayList<>(), "", Arrays.asList(content.get(1))));
                    linkingVO.getWhere().add(whereSingle);
                    linkingVOMap.put(seg+content.get(0), linkingVO);
                }
            }
        }
        for(Map.Entry entry : linkingVOMap.entrySet()) {
            result.add((LinkingVO)entry.getValue());
        }
        for(LinkingVO l : result) {
            Entity e = neoManager.getEntityFromUri(l.getUri());
            l.setAbstractMsg(e.getAbstractMsg());
            l.setClassList(e.getClassList());
        }
        return result;
    }
}
