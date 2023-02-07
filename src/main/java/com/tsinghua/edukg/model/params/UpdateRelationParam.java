package com.tsinghua.edukg.model.params;

import com.tsinghua.edukg.model.Relation;
import lombok.Data;

/**
 * 编辑图谱关系接口参数
 *
 * @author tanzheng
 * @date 2022/10/12
 */

@Data
public class UpdateRelationParam {

    Relation prev;

    Relation next;
}
