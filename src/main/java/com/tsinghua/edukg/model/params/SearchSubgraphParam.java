package com.tsinghua.edukg.model.params;

import lombok.Data;

import java.util.List;

/**
 * 搜索子图接口参数
 *
 * @author tanzheng
 * @date 2022/10/12
 */

@Data
public class SearchSubgraphParam {

    List<String> instanceList;
}
