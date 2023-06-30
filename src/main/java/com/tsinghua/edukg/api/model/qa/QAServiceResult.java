package com.tsinghua.edukg.api.model.qa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QAServiceResult {

    double modelScore;

    QAResult qaResult;
}
