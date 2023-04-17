package com.tsinghua.edukg.tools;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tsinghua.edukg.dao.entity.ZYKHtml;
import com.tsinghua.edukg.dao.mapper.ZYKHtmlMapper;
import com.tsinghua.edukg.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootTest
@Slf4j
public class HtmlParser {

    @Resource
    ZYKHtmlMapper zykHtmlMapper;

    @Test
    public void gen() throws IOException {
        String basePath = "./anoData/prop/";
        String propOut = "./propOut.txt";
        String jsonOut = "./jsonOut.json";
        File file = new File(propOut);
        File file1 = new File(jsonOut);
        FileWriter fileWriter = new FileWriter(file.getName(), false);
        FileWriter fileWriter1 = new FileWriter(file1.getName(), false);
        List<String> fileNames = CommonUtil.readDir(basePath);
        StringBuilder sb = new StringBuilder();
        Set<String> entities = new HashSet<>();
        for(String fileName : fileNames) {
            List<String> contents = CommonUtil.readPlainTextFile(basePath + fileName);
            Set<String> subjectCls = new HashSet<>();
            for(String content : contents) {
                try {
                    String subjectUrl = content.split(" ")[2];
                    String subjectName = content.split(" ")[0];
                    List<String> cls = Arrays.asList(content.split(" ")[1].split(","));
                    String objectUrl = content.split(" ")[4];
                    String objectName = content.split(" ")[3];
                    String htmlPath = "D:\\";
                    if(!subjectUrl.split("#xpointer")[0].split("/")[1].equals(objectUrl.split("#xpointer")[0].split("/")[1])) {
                        CommonUtil.failedRecord(content);
                        continue;
                    }
                    int index = Integer.parseInt(subjectUrl.split("#xpointer")[0].split("label/")[1]);
                    ZYKHtml zykHtml = zykHtmlMapper.selectByPrimaryKey(index);
                    htmlPath += zykHtml.getFilePath().replace("/", "\\");
                    File htmlFile = new File(htmlPath);
                    Document document = Jsoup.parse(htmlFile, "UTF-8");
                    SourceInfo outerSource = getAlignMsg(subjectUrl, objectUrl, document);
                    SourceInfo objectSource = getSource(objectUrl, document, objectName);
                    SourceInfo subjectSource = getSource(subjectUrl, document, subjectName);
                    if(outerSource == null || subjectSource == null) {
                        CommonUtil.failedRecord(content);
                        continue;
                    }
                    alignIndexNew(outerSource, objectSource, subjectSource);
                    entities.add(subjectName);
                    subjectCls.addAll(cls);
                    Outer outer = genOuter(cls, subjectSource, objectSource, outerSource, fileName);
                    fileWriter1.write(JSON.toJSONString(outer, SerializerFeature.DisableCircularReferenceDetect) + "\n");
                    fileWriter1.flush();
                    System.out.println(JSON.toJSONString(outer, SerializerFeature.DisableCircularReferenceDetect));
                }
                 catch (Exception e) {
                     e.printStackTrace();
                 }
            }
            sb.append(fileName.replace(".txt", "") + "：\n");
            sb.append("主体：");
            for(String clsName : subjectCls) {
                sb.append(clsName + "|");
            }
            sb.append("\n客体：属性\n");

        }
        StringBuilder outerSb = new StringBuilder();
        outerSb.append("实体：\n");
        for(String entityName : entities) {
            outerSb.append(entityName + "\n");
        }
        outerSb.append("关系：\n");
        outerSb.append(sb);
        fileWriter.write(outerSb.toString());
        fileWriter.flush();
        fileWriter.close();
        fileWriter1.close();

//        try {
//            File htmlFile = new File("Chapter_01_62_知识4中国古代作家简介.html");
//            Document document = Jsoup.parse(htmlFile, "UTF-8");
//            String subjectUrl = "http://kb.cs.tsinghua.edu.cn/apibztask/label/3663#xpointer(start-point(string-range(//BODY/P[64]/SPAN[1]/text()[1],'',3))/range-to(string-range(//BODY/P[64]/SPAN[1]/text()[1],'',5)))";
//            String subjectName = "宋濂";
//            String objectUrl = "http://kb.cs.tsinghua.edu.cn/apibztask/label/3663#xpointer(start-point(string-range(//BODY/P[64]/text()[1],'',19))/range-to(string-range(//BODY/P[64]/text()[1],'',21)))";
//            String objectName = "景濂";
//            SourceInfo outerSource = getAlignMsg(subjectUrl, objectUrl, document);
//            SourceInfo objectSource = getSource(objectUrl, document, objectName);
//            SourceInfo subjectSource = getSource(subjectUrl, document, subjectName);
//            if(outerSource == null || subjectSource == null) {
//                CommonUtil.failedRecord();
//            }
//            alignIndex(outerSource, objectSource, subjectSource);
//            System.out.println(subjectSource.getRawText().substring(24, 26));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public Outer genOuter(List<String> cls, SourceInfo subjectSource, SourceInfo objectSource, SourceInfo outerSource, String fileName) {
        Entity subjectEntity = Entity.builder()
                .text(subjectSource.getName())
                .type(cls)
                .offset(genOffset(subjectSource.getStartPos(), subjectSource.getEndPos()))
                .build();
        Entity objectEntity = Entity.builder()
                .text(objectSource.getName())
                .type(Arrays.asList("属性"))
                .offset(genOffset(objectSource.getStartPos(), objectSource.getEndPos()))
                .build();
        List<Entity> args = new ArrayList<>();
        args.add(subjectEntity);
        args.add(objectEntity);
        Relation relation = Relation.builder()
                .type(fileName.replace(".txt", ""))
                .args(args)
                .build();
        List<Relation> relations = new ArrayList<>();
        relations.add(relation);
        Outer outer = Outer.builder()
                .text(outerSource.getRawText())
                .entity(args)
                .relation(relations)
                .build();
        return outer;
    }

    public List<Integer> genOffset(int start, int end) {
        List<Integer> ret = new ArrayList<>();
        for(int i=start; i<end; i++) {
            ret.add(i);
        }
        return ret;
    }

    public static void alignIndex(SourceInfo outerSource, SourceInfo objectSource, SourceInfo subjectSource) {
        objectSource.setStartPos(outerSource.getAlignLen() + objectSource.getAlignLen() + objectSource.getStartPos());
        objectSource.setEndPos(outerSource.getAlignLen() + objectSource.getAlignLen() + objectSource.getEndPos());
        subjectSource.setEndPos(outerSource.getAlignLen() + subjectSource.getAlignLen() + subjectSource.getEndPos());
        subjectSource.setEndPos(outerSource.getAlignLen() + subjectSource.getAlignLen() + subjectSource.getEndPos());
        objectSource.setRawText(outerSource.getRawText());
        subjectSource.setRawText(outerSource.getRawText());
    }
    public static void alignIndexNew(SourceInfo outerSource, SourceInfo objectSource, SourceInfo subjectSource) {
        String rawText = outerSource.getRawText();
        String subject = subjectSource.getName();
        String object = objectSource.getName();
        objectSource.setStartPos(rawText.split(object)[0].length());
        objectSource.setEndPos(rawText.split(object)[0].length() + object.length());
        subjectSource.setEndPos(rawText.split(subject)[0].length());
        subjectSource.setEndPos(rawText.split(subject)[0].length() + subject.length());
    }
    public static SourceInfo getSource(String url, Document document, String name) {
//        url = "http://kb.cs.tsinghua.edu.cn/apibztask/label/243#xpointer(start-point(string-range(//BODY/P[3]/text()[1],'',1))/range-to(string-range(//BODY/P[3]/text()[1],'',3)))";
        String template = url.split("BODY/")[1].split("/text\\(\\)")[0];
        Element spanElement = document.select("body").first();
        for(String atom : template.split("/")) {
            spanElement = spanElement.select(atom.split("\\[")[0]).get(Integer.parseInt(atom.split("\\[")[1].split("\\]")[0]) - 1);
        }
        int start = Integer.parseInt(url.split("\\)\\)")[0].split("'',")[1]);
        int end = Integer.parseInt(url.split("\\)\\)\\)")[0].split("'',")[2]);
        String rawText = "", parseName = "";
        int alignLen = 0;
        for(Node child : spanElement.childNodes()) {
            if(child instanceof TextNode) {
                rawText = ((TextNode) child).text().toString();
                parseName = ((TextNode) child).text().toString().substring(start, end);
                break;
            }
            String childText = child.toString();
            for(String s : CommonUtil.getMiddleTextFromTags(childText, "<", ">")) {
                childText = childText.replace(s, "");
            }
            alignLen += childText.length();
        }
        if(name.equals(parseName)) {
            return SourceInfo.builder()
                    .startPos(start)
                    .name(name)
                    .endPos(end)
                    .rawText(rawText)
                    .element(spanElement)
                    .alignLen(alignLen)
                    .build();
        }
        return null;
    }

    public static SourceInfo getAlignMsg(String subjectUrl, String objectUrl, Document document) {
        String[] subjectTempArray = subjectUrl.split("BODY/")[1].split("/text\\(\\)")[0].split("/");
        String[] objectTempArray = objectUrl.split("BODY/")[1].split("/text\\(\\)")[0].split("/");
        Element spanElement = document.select("body").first();
        int len = Math.min(subjectTempArray.length, objectTempArray.length);
        String retText = "";
        boolean startTag = false;
        int tailBais = 0;
        for(int i=0; i<len; i++) {
            String name = "";
            int index = 0;
            if(!subjectTempArray[i].equals(objectTempArray[i])) {
                if(i == 0) {
                    String subjectName = subjectTempArray[i].split("\\[")[0];
                    String objectName = objectTempArray[i].split("\\[")[0];
                    int subjectIndex = Integer.parseInt(subjectTempArray[i].split("\\[")[1].split("\\]")[0]) - 1;
                    int objectIndex = Integer.parseInt(objectTempArray[i].split("\\[")[1].split("\\]")[0]) - 1;
                    String startText = spanElement.select(subjectName).get(subjectIndex).text();
                    String endText = spanElement.select(objectName).get(objectIndex).text();
                    for(Element element : spanElement.children()) {
                        if(element.text().equals(endText)) {
                            tailBais = retText.length();
                            retText += element.text();
                            break;
                        }
                        if(startTag) {
                            retText += element.text();
                        }
                        else if(element.text().equals(startText)) {
                            startTag = true;
                            retText += startText;
                        }
                    }
                }
                break;
            }
            name = subjectTempArray[i].split("\\[")[0];
            index = Integer.parseInt(subjectTempArray[i].split("\\[")[1].split("\\]")[0]) - 1;
            spanElement = spanElement.select(name).get(index);
        }
        if(retText.equals("")) {
            retText = spanElement.text();
        }
        return SourceInfo.builder()
                .rawText(retText)
                .alignLen(tailBais)
                .build();
    }

    public static Element getCommonElement(Element subjectElement, Element objectElement) {
        if(subjectElement.text().equals(objectElement.text())) {
            return subjectElement;
        }
        if(subjectElement.parent().text().equals(objectElement.text())) {
            return objectElement;
        }
        if(objectElement.parent().text().equals(subjectElement.text())) {
            return subjectElement;
        }
        if(!subjectElement.tag().getName().equals("body")) {
            subjectElement = subjectElement.parent();
        }
        if(!objectElement.tag().getName().equals("body")) {
            objectElement = objectElement.parent();
        }
        return getCommonElement(subjectElement, objectElement);
    }
}
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class SourceInfo {

    int startPos;

    int endPos;

    String name;

    String rawText;

    Element element;

    int alignLen;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Entity {

    List<String> type;

    List<Integer> offset;

    String text;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Relation {

    String type;

    List<Entity> args;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Outer {

    String text;
    List<Entity> entity;
    List<Relation> relation;
}

