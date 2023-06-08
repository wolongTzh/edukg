package com.tsinghua.edukg.service;

import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.EntityWithSource;
import com.tsinghua.edukg.model.VO.LinkingVO;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.params.HotEntitiesParam;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.model.params.SearchSubgraphParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * graph service
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Service
public interface GraphService {

    Entity getEntityFromUri(String uri);
    EntityWithSource getEntityWithSourceFromUri(String uri) throws IOException;
    List<Entity> getEntityFromClass(String className);

    List<Entity> getEntityFromSubject(String subject) throws IOException;

    void updateSubjectGraph() throws IOException;

    List<Entity> getEntityFromName(String searchText);

    List<Entity> getHotEntities(HotEntitiesParam param);

    List<Relation> searchSubgraph(SearchSubgraphParam param);

    List<LinkingVO> linkingEntities(LinkingParam param);
}
