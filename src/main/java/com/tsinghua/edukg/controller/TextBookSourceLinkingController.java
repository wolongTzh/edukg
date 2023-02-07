package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.controller.utils.TextBookSourceLinkingControllerUtil;
import com.tsinghua.edukg.model.TextBook;
import com.tsinghua.edukg.model.VO.GetTextBookHighLightVO;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.model.params.GetTextBookHighLightParam;
import com.tsinghua.edukg.service.TextBookLinkingService;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 教材资源读取 controller
 *
 * @author tanzheng
 * @date 2022/11/21
 */
@RestController
@RequestMapping(value = "api/resource")
@Slf4j
public class TextBookSourceLinkingController {

    @Autowired
    TextBookLinkingService textBookLinkingService;

    /**
     * 通过id查找教材资源
     *
     * @return
     */
    @GetMapping(value = "getBookData")
    public WebResInfo getBookData(String bookId) throws IOException {
        TextBook textBook = textBookLinkingService.getTextBookFromId(bookId);
        return WebUtil.successResult(textBook);
    }

    /**
     * 通过文本匹配教材简略信息
     *
     * @return
     */
    @GetMapping(value = "findBook")
    public WebResInfo findCourse(GetTextBookHighLightParam param) throws IOException {
        TextBookSourceLinkingControllerUtil.validGetTextBookHighLightParam(param);
        GetTextBookHighLightVO getTextBookHighLightVO = textBookLinkingService.getHighLightMsg(param);
        return WebUtil.successResult(getTextBookHighLightVO);
    }
}
