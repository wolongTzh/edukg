package com.tsinghua.edukg.model.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试题资源读取接口参数
 *
 * @author tanzheng
 * @date 2022/11/3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetExamSourceParam {

    String uri;

    String searchText;

    Integer pageNo;

    Integer pageSize;
}
