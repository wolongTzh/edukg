package com.tsinghua.edukg.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tsinghua.edukg.dao.entity.ZYKHtml;
import com.tsinghua.edukg.dao.mapper.ZYKHtmlMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class HtmlParserUtil {

    @Resource
    ZYKHtmlMapper zykHtmlMapper;

    public JSONObject mainProcess(String sparqlMsg, String name, int type) throws IOException {
        JSONObject jsonObject = new JSONObject();
        if(type == 0) {
            jsonObject = genProp(sparqlMsg, name);
        }
        else {
            jsonObject = genRela(sparqlMsg, name);
        }
        return jsonObject;
    }

    public JSONObject genProp(String sparqlMsg, String name) throws IOException {
        Outer outer = new Outer();
        try {
            String subjectUrl = sparqlMsg.split(" ")[1];
            String subjectName = sparqlMsg.split(" ")[0];
            String objectUrl = sparqlMsg.split(" ")[3];
            String objectName = sparqlMsg.split(" ")[2];
            String htmlPath = "/data/textbook";
            if(!subjectUrl.split("#xpointer")[0].split("/")[1].equals(objectUrl.split("#xpointer")[0].split("/")[1])) {
                return JSON.parseObject(JSON.toJSONString(outer));
            }
            int index = Integer.parseInt(subjectUrl.split("#xpointer")[0].split("label/")[1]);
            ZYKHtml zykHtml = zykHtmlMapper.selectByPrimaryKey(index);
            htmlPath += zykHtml.getFilePath().replace("/epub", "");
            File htmlFile = new File(htmlPath);
            Document document = Jsoup.parse(htmlFile, "UTF-8");
            SourceInfo outerSource = getAlignMsg(subjectUrl, objectUrl, document);
            SourceInfo objectSource = getSource(objectUrl, document, objectName);
            SourceInfo subjectSource = getSource(subjectUrl, document, subjectName);
            if(outerSource == null || subjectSource == null) {
                return JSON.parseObject(JSON.toJSONString(outer));
            }
            alignIndexNew(outerSource, objectSource, subjectSource);
            outer = genOuterProp(subjectSource, objectSource, outerSource, name);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.parseObject(JSON.toJSONString(outer));
    }

    public JSONObject genRela(String sparqlMsg, String name) throws IOException {
        Outer outer = new Outer();
        try {
            String subjectUrl = sparqlMsg.split(" ")[1];
            String subjectName = sparqlMsg.split(" ")[0];
            String objectUrl = sparqlMsg.split(" ")[3];
            String objectName = sparqlMsg.split(" ")[2];
            String htmlPath = "/data/textbook";
            if(!subjectUrl.split("#xpointer")[0].split("/")[1].equals(objectUrl.split("#xpointer")[0].split("/")[1])) {
                return JSON.parseObject(JSON.toJSONString(outer));
            }
            int index = Integer.parseInt(subjectUrl.split("#xpointer")[0].split("label/")[1]);
            ZYKHtml zykHtml = zykHtmlMapper.selectByPrimaryKey(index);
            htmlPath += zykHtml.getFilePath().replace("/epub", "");
            File htmlFile = new File(htmlPath);
            Document document = Jsoup.parse(htmlFile, "UTF-8");
            SourceInfo outerSource = getAlignMsg(subjectUrl, objectUrl, document);
            SourceInfo objectSource = getSource(objectUrl, document, objectName);
            SourceInfo subjectSource = getSource(subjectUrl, document, subjectName);
            if(outerSource == null || subjectSource == null) {
                return JSON.parseObject(JSON.toJSONString(outer));
            }
            alignIndexNew(outerSource, objectSource, subjectSource);
            outer = genOuterRela(subjectSource, objectSource, outerSource, name);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.parseObject(JSON.toJSONString(outer));
    }

    public Outer genOuterProp(SourceInfo subjectSource, SourceInfo objectSource, SourceInfo outerSource, String name) {
        Entity subjectEntity = Entity.builder()
                .text(subjectSource.getName())
                .offset(genOffset(subjectSource.getStartPos(), subjectSource.getEndPos()))
                .build();
        Entity objectEntity = Entity.builder()
                .text(objectSource.getName())
                .offset(genOffset(objectSource.getStartPos(), objectSource.getEndPos()))
                .build();
        List<Entity> args = new ArrayList<>();
        args.add(subjectEntity);
        args.add(objectEntity);
        Relation relation = Relation.builder()
                .type(name)
                .args(args)
                .build();
        List<Relation> relations = new ArrayList<>();
        relations.add(relation);
        Outer outer = Outer.builder()
                .text(outerSource.getRawText())
                .relation(relations)
                .build();
        return outer;
    }

    public Outer genOuterRela(SourceInfo subjectSource, SourceInfo objectSource, SourceInfo outerSource, String name) {
        Entity subjectEntity = Entity.builder()
                .text(subjectSource.getName())
                .offset(genOffset(subjectSource.getStartPos(), subjectSource.getEndPos()))
                .build();
        Entity objectEntity = Entity.builder()
                .text(objectSource.getName())
                .offset(genOffset(objectSource.getStartPos(), objectSource.getEndPos()))
                .build();
        List<Entity> args = new ArrayList<>();
        args.add(subjectEntity);
        args.add(objectEntity);
        Relation relation = Relation.builder()
                .type(name)
                .args(args)
                .build();
        List<Relation> relations = new ArrayList<>();
        relations.add(relation);
        Outer outer = Outer.builder()
                .text(outerSource.getRawText())
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

    public static void alignIndexNew(SourceInfo outerSource, SourceInfo objectSource, SourceInfo subjectSource) {
        String rawText = outerSource.getRawText();
        String subject = subjectSource.getName();
        String object = objectSource.getName();
        objectSource.setStartPos(rawText.split(object)[0].length());
        objectSource.setEndPos(rawText.split(object)[0].length() + object.length());
        subjectSource.setStartPos(rawText.split(subject)[0].length());
        subjectSource.setEndPos(rawText.split(subject)[0].length() + subject.length());
    }

    public static SourceInfo getSource(String url, Document document, String name) {
//        url = "http://kb.cs.tsinghua.edu.cn/apibztask/label/243#xpointer(start-point(string-range(//BODY/P[3]/text()[1],'',1))/range-to(string-range(//BODY/P[3]/text()[1],'',3)))";
        String template = url.split("BODY/")[1].split("/text\\(\\)")[0];
        Element spanElement = document.select("body").first();
        for(String atom : template.split("/")) {
            try {
                spanElement = spanElement.select(atom.split("\\[")[0]).get(Integer.parseInt(atom.split("\\[")[1].split("\\]")[0]) - 1);
            }
            catch (Exception e) {
                continue;
            }
        }
        int start = 0;
        int end = 0;
        try {
            start = Integer.parseInt(url.split("\\)\\)")[0].split("'',")[1]);
            end = Integer.parseInt(url.split("\\)\\)\\)")[0].split("'',")[2]);
        }
        catch (Exception e) {
            return null;
        }
        String rawText = "", parseName = "";
        int alignLen = 0;
        for(Node child : spanElement.childNodes()) {
            if(child instanceof TextNode) {
                rawText = ((TextNode) child).text().toString();
                try {
                    parseName = ((TextNode) child).text().toString().substring(start, end);
                }
                catch (Exception e) {
                    continue;
                }
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
    List<Relation> relation;
}