package com.tsinghua.edukg.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.config.RedisConfig;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;

/**
 * graph service impl
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Service
@Slf4j
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

    String blackListPath = "static/linkingBlackList.out";

    String subjectGraphPath;

    Map<String, String> linkingContentMap = new HashMap<>();

    String splitTag = "@splitTag@";

    String sourcePath = "";

    List<String> blackList = new ArrayList<>();

    @Autowired
    public GraphServiceImpl(RedisConfig redisConfig, AddressConfig addressConfig) throws IOException {
        this.subjectGraphPath = addressConfig.getSubjectGraph();
        openGate = redisConfig.getOpenGate();
        String path = linkingPath;
        sourcePath = addressConfig.getSourcePath();
        List<String> contentList = CommonUtil.readTextInResource(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path)));
        blackList = CommonUtil.readTextInResource(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(blackListPath)));;
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
        else {
            return null;
        }
        List<String> pager = XpointerUtil.getPager(html, pointer);
        index = Integer.parseInt(htmlPath.split("epub/")[1].split("/Text")[0]);
        String cover = sourcePath + String.format("/epub/%s/Images/Cover.jpg", index);
        String content = sourcePath + String.format("/epubimg/%s/%s.jpg", index, pager.get(0));
        File contentImg = new File(content);
        if(!contentImg.exists()) {
            content = null;
        }
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
    public List<Entity> getEntityFromSubject(String subject) throws IOException {
        if(!new File(String.format(subjectGraphPath, subject)).exists()) {
            throw new BusinessException(BusinessExceptionEnum.SUBJECT_NOT_EXIST);
        }
        JSONArray jsonArray = CommonUtil.readJsonArray(String.format(subjectGraphPath, subject));
        return JSONObject.parseArray(jsonArray.toJSONString(), Entity.class);
    }
    @Override
    public void updateSubjectGraph() throws IOException {
        Map<String, String> subjectMap = RuleHandler.grepSubjectMap();
        for(Map.Entry entry : subjectMap.entrySet()) {
            String subject = (String) entry.getKey();
            log.info("更新" + subject + "学科图谱。。。");
            List<Entity> entityList = neoManager.getEntityListFromClass(RuleHandler.convertSubject2Label(subject));
            List<Entity> retEntityList = new ArrayList<>();
            int count = 0;
            for(Entity entity : entityList) {
                count++;
                retEntityList.add(neoManager.getEntityFromUri(entity.getUri()));
            }
            File file = new File(String.format(subjectGraphPath, subject));
            FileWriter fileWriter = new FileWriter(file.getAbsolutePath(), false);
            fileWriter.write(JSON.toJSONString(retEntityList));
            fileWriter.close();
        }
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
        List<Relation> relations = neoManager.findPathBetweenNodesFromReslib(instanceList.get(0), instanceList.get(1));
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
        Map<String, List<Entity>> sourceMap = new HashMap<>();
        int start = 0;
        int end = 0;
        Map<String, LinkingVO> linkingVOMap = new HashMap<>();
        for(String seg : segResult) {
            start = end;
            end += seg.length();
            if(seg.length() < 2) {
                continue;
            }
            List<Entity> entityList = new ArrayList<>();
            if(sourceMap.containsKey(seg)) {
                entityList = sourceMap.get(seg);
            }
            else {
                entityList = neoManager.getEntityListFromName(seg);
                sourceMap.put(seg, entityList);
            }
            if(entityList.size() != 0) {
                Entity entity = entityList.get(0);
                String uri = entity.getUri();
                List<Integer> whereSingle = Arrays.asList(start, end);
                LinkingVO linkingVO = linkingVOMap.getOrDefault(seg+uri, new LinkingVO(seg, uri, new ArrayList<>(), "", entity.getClassList()));
                linkingVO.getWhere().add(whereSingle);
                linkingVOMap.put(seg+uri, linkingVO);
            }
        }
        for(Map.Entry entry : linkingVOMap.entrySet()) {
            result.add((LinkingVO)entry.getValue());
        }
        List<LinkingVO> finalResult = new ArrayList<>();
        for(LinkingVO l : result) {
            boolean flag = false;
            if(l.getName().length() > 1) {
                flag = true;
            }
            Entity entity = neoManager.getEntityFromUri(l.getUri());
            RuleHandler.propertyConverter(entity.getProperty());
            if(!blackList.contains(l.getName()) && flag && entity.getProperty().size() > 1) {
                finalResult.add(l);
            }
        }
        return finalResult;
    }
}
