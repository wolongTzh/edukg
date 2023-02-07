package com.tsinghua.edukg.model;

import lombok.Data;

/**
 * 教材资源实体
 *
 * @author tanzheng
 * @date 2022/11/8
 */
@Data
public class TextBook {

    String bookName;

    String subject;

    String edition;

    String editionTime;

    String isbn;

    String htmlName;

    String html;
}
