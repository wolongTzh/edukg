package com.tsinghua.edukg.service.impl;

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
import com.tsinghua.edukg.service.GraphService;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.JiebaHelper;
import com.tsinghua.edukg.utils.RuleHandler;
import org.apache.commons.lang3.StringUtils;
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

    String linkingPath = "static/concept_entities.csv";

    Map<String, String> linkingContentMap = new HashMap<>();

    String splitTag = "@splitTag@";

    @Autowired
    public GraphServiceImpl(RedisConfig redisConfig) throws IOException {
        openGate = redisConfig.getOpenGate();
        String path = linkingPath;
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
            String add = spliter[uri] + splitTag + spliter[cls];
            linkingContentMap.putIfAbsent(spliter[label], add);
        }
        System.out.println(1);
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
    public List<Entity> getEntityFromClass(String className) {
        List<Entity> entityList = neoManager.getEntityListFromClass(className);
        return entityList;
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
        if(relations.size() > 10) {
            relations = relations.subList(0, 11);
        }
        return relations;
    }

    @Override
    public List<LinkingVO> linkingEntities(LinkingParam param) {
        List<LinkingVO> result = new ArrayList<>();
        String text = param.getSearchText();
        if(StringUtils.isEmpty(text)) {
            return result;
        }
        List<String> segResult = segmenter.cutWords(text);
        Map<String, String> sourceMap = linkingContentMap;
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
                List<String> content = Arrays.asList(sourceMap.get(seg).split(splitTag));
                List<Integer> whereSingle = Arrays.asList(start, end);
                LinkingVO linkingVO = linkingVOMap.getOrDefault(seg+content.get(0), new LinkingVO(seg, content.get(0), new ArrayList<>(), "", RuleHandler.classConverter(Arrays.asList(content.get(1)))));
                linkingVO.getWhere().add(whereSingle);
                linkingVOMap.put(seg+content.get(0), linkingVO);

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
