package com.tsinghua.edukg.model.params;

import com.tsinghua.edukg.model.Property;
import lombok.Data;

/**
 * 编辑图谱实体属性接口参数
 *
 * @author tanzheng
 * @date 2022/10/12
 */

@Data
public class UpdatePropertyParam {

    String uri;

    Property prev;

    Property next;
}
