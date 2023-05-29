package com.tsinghua.edukg.model.VO;

import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.EntitySimp;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CombineLinkingVO {

    Entity instanceInfo;

    List<EntitySimp> instanceList;

    GetTextBookHighLightVO bookList;

    GetExamSourceVO questionList;

    Object courseList;

    PredicateSearchVO predicateSearchVO;
}
