package com.tsinghua.edukg.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * class内部
 *
 * @author tanzheng
 * @date 2023/02/09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassInternal {

    String id;

    String label;
}
