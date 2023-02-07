package com.tsinghua.edukg.model.DTO;

import com.tsinghua.edukg.model.Entity;
import lombok.Data;

import java.util.List;

/**
 * 批量增加实体DTO参数
 *
 * @author tanzheng
 * @date 2022/10/20
 */

@Data
public class QuikAddEntitiesDTO {

    String subject;

    String label;

    List<Entity> entities;
}
