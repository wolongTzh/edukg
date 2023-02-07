package com.tsinghua.edukg.model.VO;

import com.tsinghua.edukg.model.ExamSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetExamSourceVO {

    Integer pageNo;

    Integer pageSize;

    Integer totalCount;

    List<ExamSource> data;
}
