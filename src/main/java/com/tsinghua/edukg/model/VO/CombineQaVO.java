package com.tsinghua.edukg.model.VO;

import com.tsinghua.edukg.api.model.qa.QAResult;
import com.tsinghua.edukg.model.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CombineQaVO {

    QAResult answer;

    Entity instanceInfo;

    GetTextBookHighLightVO bookList;

    GetExamSourceVO questionList;

    Object courseList;

    List<QAESGrepVO> qaesGrepVO;

    String consistentAnswer;
}
