package com.tsinghua.edukg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class TextBookHighLight {

    String bookId;

    String example;

    double score;
}
