package com.tsinghua.edukg.model.VO;

import com.tsinghua.edukg.model.ClassInternal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkingVO {

    String name;

    String uri;

    List<List<Integer>> where;

    String abstractMsg;

    List<ClassInternal> classList;
}
