package com.tsinghua.edukg.api.model.qa;

import lombok.Data;

@Data
public class QAParseResult {
    QAParseBody no_constraint;
    QAParseBody constraint;
}
