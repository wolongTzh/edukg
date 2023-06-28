package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语义解析出来的主语和谓词，这里主要是用于前处理解析
 *
 * @author tanzheng
 * @date 2023/06/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubAndPre {

    String subject;

    String predicate;
}
