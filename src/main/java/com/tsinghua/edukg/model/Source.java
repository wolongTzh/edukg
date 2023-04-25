package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图谱来源信息
 *
 * @author tanzheng
 * @date 2023/04/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Source {

    String cover;

    String content;
}
