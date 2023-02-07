package com.tsinghua.edukg.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QAESGrepVO {

    String text;

    List<LinkingVO> linkingVOList;

    String bookId;
}
