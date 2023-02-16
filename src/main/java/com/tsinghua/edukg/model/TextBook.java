package com.tsinghua.edukg.model;

import lombok.Data;

import java.util.List;

/**
 * 教材资源实体
 *
 * @author tanzheng
 * @date 2022/11/8
 */
@Data
public class TextBook {

    List<TextBookHighLight> chapterList;

    String bookName;

    String subject;

    String edition;

    String editionTime;

    String isbn;

    String htmlName;

    String html;

    String picBasePath;
}
