package com.tsinghua.edukg.service;

import com.tsinghua.edukg.model.TextBook;
import com.tsinghua.edukg.model.VO.GetTextBookHighLightVO;
import com.tsinghua.edukg.model.params.GetTextBookHighLightParam;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface TextBookLinkingService {

    GetTextBookHighLightVO getHighLightMsg(GetTextBookHighLightParam param) throws IOException;

    TextBook getTextBookFromId(String id) throws IOException;
}
