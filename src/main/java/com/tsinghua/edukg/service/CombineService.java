package com.tsinghua.edukg.service;

import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.model.VO.CombineLinkingVO;
import com.tsinghua.edukg.model.VO.CombineQaVO;
import com.tsinghua.edukg.model.params.TotalSearchParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * 综合搜索 service
 *
 * @author tanzheng
 * @date 2022/11/29
 */
@Service
public interface CombineService {

    CombineLinkingVO totalSearch(TotalSearchParam param) throws IOException;

    CombineQaVO totalQa(QAParam qaParam) throws IllegalAccessException, IOException, ExecutionException, InterruptedException;
}
