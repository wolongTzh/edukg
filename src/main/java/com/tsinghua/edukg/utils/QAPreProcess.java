package com.tsinghua.edukg.utils;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.model.SubAndPre;


import java.util.Arrays;
import java.util.List;

public class QAPreProcess {

    public static SubAndPre mainProcess(String question) {
        SubAndPre subAndPre = new SubAndPre();
        // 成语处理
        if(question.contains("什么意思")) {
            return idiomHandle(question);
        }
        // 古诗前后句处理
        List<String> poetryCase = Arrays.asList("前一句", "上一句", "后一句", "下一句");
        for(String str : poetryCase) {
            if(question.contains(str)) {
                return poetryHandle(question, str);
            }
        }
        String subject = analyseSubject(question);
        if(!subject.equals("")) {
            subAndPre.setSubject(subject);
        }
        String predicate = analysePredicate(question);
        if(!predicate.equals("")) {
            subAndPre.setPredicate(predicate);
        }
        return subAndPre;
    }

    public static String analysePredicate(String question) {
        if(question.contains("什么家")) {
            return "职业";
        }
        if(question.contains("默写")) {
            return "内容";
        }
        if(question.contains("字什么")) {
            return "字";
        }
        if(question.contains("号什么")) {
            return "号";
        }
        if(question.contains("选自哪")) {
            return "来源";
        }
        if(question.contains("主角")) {
            return "相关人物";
        }
        return "";
    }

    public static String analyseSubject(String question) {
        String preTag = "";
        String postTag = "";
        boolean retainInner = false;
        if(question.contains("《") && question.contains("》")) {
            preTag = "《";
            postTag = "》";
        }
        else if(question.contains("“") && question.contains("”")) {
            preTag = "“";
            postTag = "”";
            retainInner = true;
        }
        else if(question.contains("'") && question.contains("'")) {
            preTag = "'";
            postTag = "'";
            retainInner = true;
        }
        List<String> resultList = CommonUtil.getMiddleTextFromTags(question, preTag, postTag);
        if(resultList.size() == 0) {
            return "";
        }
        if(retainInner) {
            return resultList.get(0).substring(1, resultList.get(0).length() - 1);
        }
        return resultList.get(0);
    }

    public static SubAndPre poetryHandle(String question, String direction) {
        SubAndPre subAndPre = new SubAndPre();
        if(direction.equals("上一句")) {
            subAndPre.setPredicate("前一句");
        }
        else if(direction.equals("下一句")) {
            subAndPre.setPredicate("后一句");
        }
        else {
            subAndPre.setPredicate(direction);
        }
        List<String> delWords = Arrays.asList("默写", "补充", "补全", "填写", "的", "写出");
        for(String word : delWords) {
            question = question.replace(word, "");
        }
        String subject = analyseSubject(question);
        if(subject.contains("《") && question.contains(subject + "中")) {
            question = question.substring(question.indexOf("中") + 1);
        }
        else if(question.contains("“") && question.contains("”")) {
            subAndPre.setSubject(subject);
        }
        else if(question.contains("'") && question.contains("'")) {
            subAndPre.setSubject(subject);
        }
        else if(question.contains("。")) {
            question = question.substring(question.indexOf("。") + 1);
        }
        if(subAndPre.getSubject() == null) {
            subAndPre.setSubject(question.split(direction)[0]);
        }
        return subAndPre;
    }

    public static SubAndPre idiomHandle(String question) {
        SubAndPre subAndPre = new SubAndPre();
        List<String> delWords = Arrays.asList("是", "成语", "这个词", "字典中");
        for(String word : delWords) {
            question = question.replace(word, "");
        }
        String subject = analyseSubject(question);
        question = question.replace("，", "");
        if(subject.equals("")) {
            subject = question.split("什么意思")[0];
        }
        if(subject.contains("的")) {
            subject = subject.split("的")[0];
        }
        subAndPre.setSubject(subject);
        subAndPre.setPredicate("定义");
        return subAndPre;
    }

    public static void main(String[] args) {
        String question = "文天祥《过零丁洋》中身世浮沉雨打萍的前一句是什么";
        String out = analyseSubject(question);

        SubAndPre subAndPre = poetryHandle(question, "前一句");
        System.out.println(JSON.toJSONString(subAndPre));
    }
}
