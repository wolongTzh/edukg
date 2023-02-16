package com.tsinghua.edukg.service;

import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.manager.ESManager;
import com.tsinghua.edukg.model.TextBook;
import com.tsinghua.edukg.model.TextBookHighLight;
import com.tsinghua.edukg.model.VO.GetTextBookHighLightVO;
import com.tsinghua.edukg.model.params.GetTextBookHighLightParam;
import com.tsinghua.edukg.utils.CommonUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 教材资源读取 service
 *
 * @author tanzheng
 * @date 2022/11/18
 */
@Service
public class TextBookLinkingServiceImpl implements TextBookLinkingService {

    @Resource
    ESManager esManager;

    @Override
    public GetTextBookHighLightVO getHighLightMsg(GetTextBookHighLightParam param) throws IOException {
        String text = param.getSearchText();
        List<TextBook> textBookList = esManager.getTextBookHighLightMsgFromTerm(text);
        if(textBookList.size() == 0) {
            return new GetTextBookHighLightVO(param.getPageNo(), param.getPageSize(), 0, textBookList);
        }
        List<TextBook> textBookHighLightListPageSplit = CommonUtil.pageHelper(textBookList, param.getPageNo() - 1, param.getPageSize());
        if(textBookHighLightListPageSplit == null) {
            throw new BusinessException(BusinessExceptionEnum.PAGE_DARA_OVERSIZE);
        }
        for(TextBook textBook : textBookHighLightListPageSplit) {
            List<TextBookHighLight> chapterList = textBook.getChapterList();
            chapterList.forEach(t -> markHighLightText(t, text));
        }
        GetTextBookHighLightVO getTextBookHighLightVO = new GetTextBookHighLightVO(param.getPageNo(), param.getPageSize(), textBookList.size(), textBookHighLightListPageSplit);
        return getTextBookHighLightVO;
    }

    @Override
    public TextBook getTextBookFromId(String id) throws IOException {
        TextBook textBook = esManager.getTextBookFromId(id);
        return textBook;
    }

    private void markHighLightText(TextBookHighLight textBookHighLight, String target) {
        String source = textBookHighLight.getExample();
        String preTag = ">";
        String postTag = "<";
        String result = CommonUtil.getMiddleTextFromTags(source, target, preTag, postTag).get(0);
        preTag = "。";
        postTag = "。";
        result = CommonUtil.getMiddleTextFromTags(result, target, preTag, postTag).get(0);
        textBookHighLight.setExample(result);
    }
}
