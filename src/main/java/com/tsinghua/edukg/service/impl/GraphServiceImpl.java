package com.tsinghua.edukg.service.impl;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.config.RedisConfig;
import com.tsinghua.edukg.constant.BusinessConstant;
import com.tsinghua.edukg.dao.entity.ZYKHtml;
import com.tsinghua.edukg.dao.mapper.ZYKHtmlMapper;
import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.manager.NeoAssisManager;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.manager.RedisManager;
import com.tsinghua.edukg.model.*;
import com.tsinghua.edukg.model.VO.LinkingVO;
import com.tsinghua.edukg.model.params.HotEntitiesParam;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.model.params.SearchSubgraphParam;
import com.tsinghua.edukg.service.GraphService;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.JiebaHelper;
import com.tsinghua.edukg.utils.RuleHandler;
import com.tsinghua.edukg.utils.XpointerUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
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

    @Resource
    ZYKHtmlMapper zykHtmlMapper;

    @Autowired
    private JiebaHelper segmenter;

    private Integer openGate;

    String linkingPath = "static/concept_entities.csv";

    Map<String, String> linkingContentMap = new HashMap<>();

    String splitTag = "@splitTag@";

    String sourcePath = "";

    @Autowired
    public GraphServiceImpl(RedisConfig redisConfig, AddressConfig addressConfig) throws IOException {
        openGate = redisConfig.getOpenGate();
        String path = linkingPath;
        sourcePath = addressConfig.getSourcePath();
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
        RuleHandler.propertyConverter(entity.getProperty());
        RuleHandler.relationConverter(entity.getRelation());
        if(entity.getUri() == null) {
            return null;
        }
        return entity;
    }

    @Override
    public EntityWithSource getEntityWithSourceFromUri(String uri) throws IOException {
        Entity entity = neoManager.getEntityFromUri(uri);
        Source source = null;
        if(entity.getUri() == null) {
            return null;
        }
        for(Property property : entity.getProperty()) {
            if(property.getPredicate().equals("edukg_prop_common__main-P10")) {
                try {
                    source = getSourceFromXpointer(property.getObject());
                }
                catch (Exception e) {

                }
            }
        }
        RuleHandler.propertyConverter(entity.getProperty());
        RuleHandler.relationConverter(entity.getRelation());
        EntityWithSource entityWithSource = EntityWithSource.builder()
                .source(source)
                .build();
        BeanUtils.copyProperties(entity, entityWithSource);
        return entityWithSource;
    }

    public Source getSourceFromXpointer(String pointer) throws IOException {
        int index = Integer.parseInt(pointer.split("#xpointer")[0].split("label/")[1]);
        ZYKHtml zykHtml = zykHtmlMapper.selectByPrimaryKey(index);
        String htmlPath = sourcePath + zykHtml.getFilePath();
        File file = new File(htmlPath);
        String html = "";
        if(file.exists()){
            Long filelength = file.length(); // 获取文件长度
            byte[] filecontent = new byte[filelength.intValue()];
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
            html = new String(filecontent, "utf-8");// 返回文件内容,默认编码
        }
        List<String> pager = XpointerUtil.getPager(html, pointer);
        index = Integer.parseInt(htmlPath.split("epub/")[1].split("/Text")[0]);
        String cover = sourcePath + String.format("/epubimg/%s/A_01_cover.jpg", index);
        String content = sourcePath + String.format("/epubimg/%s/%s.jpg", index, pager.get(0));
        return Source.builder()
                .cover(cover)
                .content(content)
                .build();
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
        RuleHandler.relationConverter(relations);
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
        String text = param.getSearchText();
        List<String> segResult = segmenter.cutWords(text);
        List<LinkingVO> result = new ArrayList<>();
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
