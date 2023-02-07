package com.tsinghua.edukg.model.VO;

import com.tsinghua.edukg.model.ExamSource;
import com.tsinghua.edukg.model.TextBookHighLight;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTextBookHighLightVO {

    Integer pageNo;

    Integer pageSize;

    Integer totalCount;

    List<TextBookHighLight> data;
}
