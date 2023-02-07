package com.tsinghua.edukg.api.model;

import lombok.Data;

@Data
public class QAResult {

    String subject;

    String subjectUri;

    String object;

    String objectUri;

    String answerValue;

    String predicate;

    String templateContent;

    int score;
}
