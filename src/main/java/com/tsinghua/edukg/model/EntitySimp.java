package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 图谱实体简略版
 *
 * @author tanzheng
 * @date 2022/11/29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class EntitySimp {

    protected String name;

    protected String uri;

    public String abstractMsg;

    protected List<String> classList;
}
