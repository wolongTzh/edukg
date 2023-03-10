package com.tsinghua.edukg.utils;

import com.tsinghua.edukg.model.ClassInternal;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则工具类（图谱相关类和属性规则映射）
 *
 * @author tanzheng
 * @date 2022/10/12
 */

public class RuleHandler {

    private static final Logger logger = LoggerFactory.getLogger(RuleHandler.class);

    static String questionJudgePath = "static/questionJudge.out";

    static List<String> questionJudgeList = new ArrayList<>();

    // 失效链接，应被过滤掉
    static List<String> invalidUrl = Arrays.asList("http://edukg.org","http://kb.cs.tsinghua.edu.cn");

    // 失效关系，应被过滤掉
    static List<String> filteredRel = Arrays.asList("edukg_prop_common__main-R1","edukg_prop_common__main-R3","edukg_prop_common__main-R10");

    static String subjectLabelTemplate = "edukg_cls_%s__main-C0";

    static String subjectUriPrefixTemplate = "http://edukg.org/knowledge/3.0/instance/%s#main-E";

    static String subjectUriPrefixTemplateRe = "http://edukg.org/knowledge/3.0/instance/%s#main-E(.*)";

    static Pattern subjectPropertyRe = Pattern.compile("http://edukg\\.org/knowledge/3\\.0/property/(.*)#");

    static Pattern subjectLabelRe = Pattern.compile("^edukg_cls_(.+)__main-C0$");

    static Pattern subjectClassRe = Pattern.compile("^edukg_cls_(.+)__main-C\\d+$");

    static Pattern sourceRe = Pattern.compile(".*#([^-]*)-");

    static Pattern subjectPrefixRe = Pattern.compile("^edukg_ins_(.*)$");

    static Pattern validLabelAbbrRe = Pattern.compile("edukg_cls_.*__.*-C.*");

    static Map<String, String> subjectZh2En = new HashMap<String, String>(){{
        put("语文", "chinese");
        put("数学", "math");
        put("英语", "english");
        put("物理", "physics");
        put("化学", "chemistry");
        put("生物", "biology");
        put("历史", "history");
        put("地理", "geo");
        put("政治", "politics");
    }};

    static Map<String, String> subjects = new HashMap<String, String>(){{
        put("chinese", "语文");
        put("math", "数学");
        put("english", "英语");
        put("physics", "物理");
        put("chemistry", "化学");
        put("biology", "生物");
        put("history", "历史");
        put("geo", "地理");
        put("politics", "政治");
    }};

    static String cls2labelPath =  "static/cls2label.json";

    static String pred2labelPath = "static/pred2label.json";

    static String prefixesPath = "static/prefixes.json";

    static String subClassOfPath = "static/subClassOf.json";

    static Map<String, String> cls2labelMap;

    static Map<String, String> pred2labelMap;

    static Map<String, String> prefixesMap;

    static Map<String, String> prefixUri2AbbrMap;

    static Map<String, List<String>> subClassOfMap;

    static Map<String, List<String>> subClassOfAbbrMap;

    static Map<String, Map<String, String>> propertyName2UriMap;

    static Map<String, List<String>> propertyMap;

    static Map<String, List<String>> relationMap;

    static {
        RuleHandler ruleHandler = new RuleHandler();
        cls2labelMap = CommonUtil.readJsonInResource(new InputStreamReader(ruleHandler.getClass().getClassLoader().getResourceAsStream(cls2labelPath)));
        pred2labelMap = CommonUtil.readJsonInResource(new InputStreamReader(ruleHandler.getClass().getClassLoader().getResourceAsStream(pred2labelPath)));
        prefixesMap = CommonUtil.readJsonInResource(new InputStreamReader(ruleHandler.getClass().getClassLoader().getResourceAsStream(prefixesPath)));
        subClassOfMap = CommonUtil.readJsonInResource(new InputStreamReader(ruleHandler.getClass().getClassLoader().getResourceAsStream(subClassOfPath)));
        try {
            questionJudgeList = CommonUtil.readTextInResource(new InputStreamReader(ruleHandler.getClass().getClassLoader().getResourceAsStream(questionJudgePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadPropertyName2Uri(pred2labelMap);
        loadGeneratePrefixes();
        loadPropertyRelationSets();
        loadSubAbbrMap();
    }

    /**
     * 判断是否输入的是一个问题
     * 方法是根据词表匹配看看是否有问题中出现的常用词语来判断
     * @param question
     * @return
     */
    public static boolean judgeQuestion(String question) {
        for(String s : questionJudgeList) {
            if(question.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * convert系列
     * 使用嵌入代码的静态变量转换值
     * 例如subject的map和format模板
     */

    public static String convertSubjectZh2En(String subject) {
        if(subjects.containsKey(subject)) {
            return subject;
        }
        return subjectZh2En.get(subject);
    }

    public static String convertSubject2Label(String subject) {
        return String.format(subjectLabelTemplate, subject);
    }

    public static String convertLabel2UriTemplate(String subject) {
        return String.format(subjectUriPrefixTemplate, subject);
    }

    /**
     * 中文属性关系名 -> 属性关系uri
     * 例子："构成" -> "http://edukg.org/knowledge/3.0/property/chemistry#main-P15"
     */

    public static String getPropertyAbbrByName(String subject, String property) {
        String uri = "";
        if(StringUtils.isEmpty(subject)) {
            return getPropertyAbbrWithoutSubject(property);
        }
        if(propertyName2UriMap.containsKey(subject) && propertyName2UriMap.get(subject).containsKey(property)) {
            uri = propertyName2UriMap.get(subject).get(property);
        }
        else if(propertyName2UriMap.containsKey("default") && propertyName2UriMap.get("default").containsKey(property)) {
            uri = propertyName2UriMap.get("default").get(property);
        }
        else if(propertyName2UriMap.containsKey("common") && propertyName2UriMap.get("common").containsKey(property)) {
            uri = propertyName2UriMap.get("common").get(property);
        }
        if(!StringUtils.isEmpty(uri)) {
            String prefixUri = uri.split("#")[0] + "#";
            String body = uri.split("#")[1];
            if(prefixUri2AbbrMap.containsKey(prefixUri)) {
                uri = prefixUri2AbbrMap.get(prefixUri) + "__" + body;
            }
        }
        return uri;
    }

    /**
     * 中文属性关系名 -> 属性关系uri（无学科版本）
     * 例子："易混辨析" -> "edukg_prop_english__main-P108"
     * @param property
     * @return
     */
    public static String getPropertyAbbrWithoutSubject(String property) {
        String uri = "";
        for(Map.Entry entry : propertyName2UriMap.entrySet()) {
            Map<String, String> map = (Map<String, String>) entry.getValue();
            uri = map.get(property);
            if(!StringUtils.isEmpty(uri)) {
                String prefixUri = uri.split("#")[0] + "#";
                String body = uri.split("#")[1];
                if(prefixUri2AbbrMap.containsKey(prefixUri)) {
                    uri = prefixUri2AbbrMap.get(prefixUri) + "__" + body;
                }
            }
        }
        return uri;
    }

    /**
     * 给出属性关系uri -> 对应的中文
     * 例子："edukg_prop_english__main-P108" -> "易混辨析"
     * @param uri
     * @return
     */
    public static String getPropertyNameByAbbr(String uri) {
        if(StringUtils.isEmpty(uri) || !uri.contains("__")) {
            return "";
        }
        String prefix = uri.split("__")[0];
        String body = uri.split("__")[1];
        if(prefixesMap.containsKey(prefix) && pred2labelMap.containsKey(prefixesMap.get(prefix) + body)) {
            return pred2labelMap.get(prefixesMap.get(prefix) + body);
        }
        return "";
    }

    public static String getPropertyNameByUri(String uri) {
        if(StringUtils.isEmpty(uri) || !uri.contains("#")) {
            return "";
        }
        String prefix = uri.split("#")[0] + "#";
        String body = uri.split("#")[1];
        if(prefixUri2AbbrMap.containsKey(prefix)) {
            return prefixUri2AbbrMap.get(prefix) + "__" + body;
        }
        return "";
    }

    public static String getClassNameByAbbr(String uri) {
        if(StringUtils.isEmpty(uri) || !uri.contains("__")) {
            return "";
        }
        String prefix = uri.split("__")[0];
        String body = uri.split("__")[1];
        if(prefixesMap.containsKey(prefix) && cls2labelMap.containsKey(prefixesMap.get(prefix) + body)) {
            return cls2labelMap.get(prefixesMap.get(prefix) + body);
        }
        return "";
    }

    public static String getSubjectByUri(String uri) {
        String uriPrefix = uri.split("#")[0] + "#";
        if(!prefixUri2AbbrMap.containsKey(uriPrefix)) {
            return null;
        }
        uriPrefix = prefixUri2AbbrMap.get(uriPrefix);
        Matcher matcher = subjectPrefixRe.matcher(uriPrefix);
        matcher.matches();
        String ret = null;
        try {
            ret = matcher.group(1);
        }
        catch (Exception e) {

        }
        return ret;
    }

    public static String getLabelAbbrByUri(String uri) {
        String prefixUri = uri.split("#")[0] + "#";
        String body = uri.split("#")[1];
        return prefixUri2AbbrMap.get(prefixUri) + "__" + body;
    }

    public static List<String> getSubjectProperties(String subject) {
        if(!propertyMap.containsKey(subject)) {
            subject = "common";
        }
        return propertyMap.get(subject);
    }

    public static List<String> getSubjectRelations(String subject) {
        if(!relationMap.containsKey(subject)) {
            subject = "common";
        }
        return relationMap.get(subject);
    }

    /**
     * find系列
     * 使用正则模板匹配信息
     */

    public static Matcher findSubjectLabel(String label) {
        Matcher matcher = subjectLabelRe.matcher(label);
        return matcher;
    }

    public static Matcher findSourceRe(String uri) {
        Matcher matcher = sourceRe.matcher(uri);
        return matcher;
    }

    public static Matcher findLabelSubject(String label) {
        Matcher matcher = subjectClassRe.matcher(label);
        return matcher;
    }

    public static Pattern findSubjectUriId(String subject) {
       return Pattern.compile(String.format(subjectUriPrefixTemplateRe, subject));
    }

    public static Matcher findLabelAbbr(String label) {
        Matcher matcher = validLabelAbbrRe.matcher(label);
        return matcher;
    }

    /**
     * generate系列
     * 使用一些代码逻辑生成需要的信息
     */

    public static List<String> generateSubjectUris(Integer maxId, String subject, Integer count) {
        String prefix = convertLabel2UriTemplate(subject);
        List<String> retList = new ArrayList<>();
        for(int i=maxId+1; i<maxId+count+1; i++) {
            retList.add(prefix + i);
        }
        return retList;
    }

    /**
     * grep系列
     * 类似于实体的get方法，提供接口获取私有变量
     */

    public static Map<String, List<String>> grepSubClassOfAbbrMap() {
        return subClassOfAbbrMap;
    }

    public static Map<String, String> grepSubjectMap() {
        return subjects;
    }

    static void loadPropertyName2Uri(Map<String, String> propertyUri2Name) {
        Map<String, Map<String, String>> retMap = new HashMap<>();
        for(Map.Entry entry : propertyUri2Name.entrySet()) {
            Matcher matcher = subjectPropertyRe.matcher((String) entry.getKey());
            String matchStr = "";
            if(matcher.find()) {
                matchStr = matcher.group(1);
            }
            String subjectName = "default";
            if(!StringUtils.isEmpty(matchStr)) {
                subjectName = matchStr;
            }
            Map<String, String> innerMap = retMap.getOrDefault(subjectName, new HashMap<>());
            innerMap.put((String) entry.getValue(), (String) entry.getKey());
            retMap.put(subjectName, innerMap);
        }
        propertyName2UriMap = retMap;
    }

    static void loadGeneratePrefixes() {
        prefixUri2AbbrMap = new HashMap<>();
        for(Map.Entry entry : prefixesMap.entrySet()) {
            prefixUri2AbbrMap.put((String) entry.getValue(), (String) entry.getKey());
        }
    }

    static void loadPropertyRelationSets() {
        propertyMap = new HashMap<>();
        relationMap = new HashMap<>();
        for(Map.Entry entry : pred2labelMap.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            Matcher matcher = subjectPropertyRe.matcher(key);
            if(!matcher.find()) {
                continue;
            }
            String subject = matcher.group(1);
            if(key.contains("#main-P")) {
                List<String> ls = propertyMap.getOrDefault(subject, new ArrayList<>());
                ls.add(value);
                propertyMap.put(subject, ls);
            }
            else if(key.contains("#main-R")) {
                List<String> ls = relationMap.getOrDefault(subject, new ArrayList<>());
                ls.add(value);
                relationMap.put(subject, ls);
            }
        }
    }

    static void loadSubAbbrMap() {
        subClassOfAbbrMap = new HashMap<>();
        for(Map.Entry entry : subClassOfMap.entrySet()) {
            List<String> subList = new ArrayList<>();
            for(String s : (List<String>)entry.getValue()) {
                subList.add(getLabelAbbrByUri(s));
            }
            subClassOfAbbrMap.put(getLabelAbbrByUri((String)entry.getKey()), subList);
        }
    }

    public static List<ClassInternal> classConverter(List<String> classList) {
        if(CollectionUtils.isEmpty(classList)) {
            return null;
        }
        List<ClassInternal> classOutList = new ArrayList<>();
        for(String cls : classList) {
            classOutList.add(ClassInternal.builder()
                    .id(cls)
                    .label(getClassNameByAbbr(cls))
                    .build());
        }
        return classOutList;
    }

    public static void propertyConverter(List<Property> properties) {
        if(CollectionUtils.isEmpty(properties)) {
            return;
        }
        Iterator<Property> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property property = iterator.next();
            property.setPredicateLabel(getPropertyNameByAbbr(property.getPredicate()));
            for(String url : invalidUrl) {
                if(property.getObject().contains(url)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public static void relationConverter(List<Relation> relations) {
        if(CollectionUtils.isEmpty(relations)) {
            return;
        }
        Iterator<Relation> iterator = relations.iterator();
        while (iterator.hasNext()) {
            Relation relation = iterator.next();
            if (filteredRel.contains(relation.getPredicate())) {
                iterator.remove();
                continue;
            }
            relation.setPredicateLabel(getPropertyNameByAbbr(relation.getPredicate()));
        }
    }

}
