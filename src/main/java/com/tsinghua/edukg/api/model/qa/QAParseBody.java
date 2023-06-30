package com.tsinghua.edukg.api.model.qa;

import lombok.Data;

import java.util.List;

@Data
public class QAParseBody {
    String title;

    String origin_pred;

    double model_score;

    List<QASinglePred> top_k;
}
