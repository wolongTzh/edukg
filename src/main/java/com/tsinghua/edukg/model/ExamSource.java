package com.tsinghua.edukg.model;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSource {

    String questionId;

    String questionInfo;

    JSONObject questionData;
}
