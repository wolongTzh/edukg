package com.tsinghua.edukg.model.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教材资源缩略信息查询接口参数
 *
 * @author tanzheng
 * @date 2022/11/3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetTextBookHighLightParam {

    String searchText;

    Integer pageNo;

    Integer pageSize;
}
