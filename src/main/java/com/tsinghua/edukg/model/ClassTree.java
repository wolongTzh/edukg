package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * class树
 *
 * @author tanzheng
 * @date 2023/02/09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassTree {


    protected String id;

    protected String label;

    List<ClassTree> childNodes;
}
