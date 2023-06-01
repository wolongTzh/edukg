package com.tsinghua.edukg.api.model;

import lombok.Data;

@Data
public class QAResult {

    String subject;

    String subjectUri;

    String object;

    String objectUri;

    String answerValue;

    String origin_pred;

    String templateContent;

    int score;

    double model_score;
}
