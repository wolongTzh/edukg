package com.tsinghua.edukg.model.VO;

import com.tsinghua.edukg.model.TextBook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextBookVO {

    Integer pageNo;

    Integer pageSize;

    Integer totalCount;

    List<TextBook> data;
}
