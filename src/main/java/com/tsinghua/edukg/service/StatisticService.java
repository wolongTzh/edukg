package com.tsinghua.edukg.service;

import com.tsinghua.edukg.model.SourceAndCount;
import com.tsinghua.edukg.model.VO.UpdateTotalStatusVO;
import com.tsinghua.edukg.model.params.GetAllEntityListParam;
import com.tsinghua.edukg.model.params.GetEntityListParam;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.params.SubjectStatParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * statistic service
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Service
public interface StatisticService {

    List<Entity> getEntityList(GetEntityListParam param);

    List<Entity> getAllEntityList(GetAllEntityListParam param);

    List<SourceAndCount> updateSource();

    public UpdateTotalStatusVO updateTotalStatus();

    public UpdateTotalStatusVO updateSubjectStatus(SubjectStatParam param);
}
