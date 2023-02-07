package com.tsinghua.edukg.model.params;

import lombok.Data;

import java.util.List;

/**
 * 编辑图谱实体label接口参数
 *
 * @author tanzheng
 * @date 2022/10/12
 */

@Data
public class UpdateLabelsParam {

    String uri;

    List<String> labels;
}
