package com.tsinghua.edukg;

import com.tsinghua.edukg.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

@SpringBootTest
@Slf4j
public class SparqlTest {
    /**
     * 1: 打印
     * 2: 写入文件
     */
    int threadShold = 1;

    /**
     * ttl文件路径
     */
    String inputPath = "./knowledge.ttl";

    /**
     * 输出txt文件路径
     */
    String outputPath = "./annotation.txt";

    @Test
    public void handler() throws IOException {
        // 作者 -> 作品
        // 欧阳修 醉翁亭记
        String authorAndPoet = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                " SELECT ?xn ?yn\n" +
                " WHERE\n" +
                "{ ?x <http://edukb.org/knowledge/0.1/property/chinese#literature> ?y.\n" +
                " ?x rdfs:label ?xn.\n"+
                " ?y rdfs:label ?yn.}";

        // 称号包含关系
        // 三曹 曹操
        String includeRelation = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix co: <http://edukb.org/knowledge/0.1/property/common#>\n" +
                " SELECT ?xn ?yn\n" +
                " WHERE\n" +
                "{ ?x co:includes ?y.\n" +
                " ?x rdfs:label ?xn.\n"+
                " ?y rdfs:label ?yn.}\n" +
                " ORDER BY ?xn";

        String story  = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix co: <http://edukb.org/knowledge/0.1/property/common#>\n" +
                " SELECT ?yn\n" +
                " WHERE\n" +
                "{ <http://edukb.org/knowledge/0.1/instance/chinese#-2d62088cc92cdb173bf208abf8187935> co:example ?yn.}";

        String pro2Source = "SELECT ?name ?uri\n" +
                " WHERE\n" +
                "{ ?uri a <http://www.w3.org/2002/07/owl#DatatypeProperty>." +
                "?uri <http://www.w3.org/2000/01/rdf-schema#label> ?name}";

        String nodeFilter = "SELECT ?y ?z\n" +
                " WHERE\n" +
                "{ <http://edukb.org/knowledge/0.1/instance/chinese#-2238b41d594489a133577ba3d2c1f54f> ?y ?z.}";

        switch (threadShold) {
            case 1:
                testSparql(nodeFilter);
                break;
            case 2:
                writeFile(pro2Source);
                break;
            default:
                break;
        }
    }
    @Test
    public void subjectFilterTrigger() throws IOException {
        String parentPath = "./allSubjectGraph";
        File file = new File(parentPath);
        if(file.isDirectory()) {
            for(String name : file.list()) {
                String path = parentPath + "/" + name;
                System.out.println(path);
                subjectFilter(path, name);
            }
        }
//        subjectFilter("./allSubjectGraph/physics_版本1.ttl", "geo_版本1.ttl");
    }
    public void subjectFilter(String inputPath, String fileName) throws IOException {

        int relationNeed = 5;
        int propNeed = 5;
        String outPath = "filterOut/" + fileName + ".out";
        File file = new File(outPath);
        FileWriter fileWriter = new FileWriter(file.getAbsolutePath());

        String nodeFilter = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?x ?y ?z ?name\n" +
                " WHERE\n" +
                "{ ?z ?x ?y." +
                "?z rdfs:label ?name.}";

        Model model = ModelFactory.createDefaultModel();
        //ttl文件路径
        model.read(inputPath);
        Query query = QueryFactory.create(nodeFilter);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        StringBuilder sb = new StringBuilder();
        ResultSet results = qe.execSelect();
        int relationCount = 0;
        int propCount = 0;
        String lastUri = "";
        String lastName = "";
        while (results.hasNext()) {
            QuerySolution querySolution = results.next();
            RDFNode nodeX = querySolution.get("x");
            RDFNode nodeY = querySolution.get("y");
            RDFNode nodeZ = querySolution.get("z");
            RDFNode nodeName = querySolution.get("name");
            String x = "";
            String y = "";
            String z = "";
            String name = "";
            if(nodeX != null) {
                x = nodeX.toString();
            }
            if(nodeY != null) {
                y = nodeY.toString();
            }
            if(nodeZ != null) {
                z = nodeZ.toString();
            }
            if(nodeName != null) {
                name = nodeName.toString();
            }
            if(x.contains("annotation") || x.contains("source") || x.contains("type")) {
                continue;
            }
            if(!z.equals(lastUri)) {
                if(propCount >= propNeed && relationCount >= relationNeed) {
                    System.out.println(propCount + " " + relationCount + " " + (lastName.equals("")?name:lastName) + " " + lastUri);
                    fileWriter.write(propCount + " " + relationCount + " " + (lastName.equals("")?name:lastName) + " " + lastUri + "\n");
                    fileWriter.flush();
                }
                relationCount = 0;
                propCount = 0;
                lastName = name;
                lastUri = z;
            }
            String typeCheck = "SELECT ?x\n" +
                    " WHERE\n" +
                    "{ <%s> a ?x.}";
            QueryExecution qeType = QueryExecutionFactory.create(String.format(typeCheck, x), model);
            ResultSet resultsType = qeType.execSelect();
            if(!resultsType.hasNext()) {
                continue;
            }
            QuerySolution querySolutionType = resultsType.next();
            RDFNode nodeType = querySolutionType.get("x");
            if(nodeType.toString().contains("ObjectProperty")) {
                relationCount++;
            }
            else {
                propCount++;
            }
        }
        fileWriter.close();
    }
    @Test
    public void getAllPre() throws IOException {

//        List<String> contents = CommonUtil.readPlainTextFile("./anoData/propertyLs.txt");
//        for(String content : contents) {
//            log.info("start：" + content + "\n");
//            String filePath = "./anoData/prop/";
//            filePath += content.split(" ")[0] + ".txt";
//            String pred = "<" + content.split(" ")[1] + ">";
//            getProLoc(pred, filePath);
//        }
        List<String> contents = CommonUtil.readPlainTextFile("./anoData/relationLs.txt");
        for(String content : contents) {
            String filePath = "./anoData/rela/";
            filePath += content.split(" ")[0] + ".txt";
            String pred = "<" + content.split(" ")[1] + ">";
            getRelationLoc(pred, filePath);
        }
    }

    public void getProLoc(String preType, String outputPath) throws IOException {
        String name2Source = "SELECT ?name ?source ?pred\n" +
                " WHERE\n" +
                "{ ?uri " + preType + " ?pred.\n" +
                " ?uri <http://www.w3.org/2000/01/rdf-schema#label> ?name." +
                " ?uri <http://edukb.org/knowledge/0.1/property/common#source> ?source.}";
        List<String> name2SourceParam = Arrays.asList("name", "source", "pred");

        String pro2Source = "SELECT ?name ?source ?name\n" +
                " WHERE\n" +
                "{ <%s> " + preType + " ?source.\n" +
                " ?source <http://www.w3.org/2000/01/rdf-schema#label> ?name.}";
        List<String> pro2SourceParam = Arrays.asList("name", "source");

        String clsQuery = "SELECT ?source ?cls\n" +
                " WHERE\n" +
                "{ ?uri <http://edukb.org/knowledge/0.1/property/common#source> ?source." +
                " ?uri a ?cls.}";

        File file = new File(outputPath);
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        List<String> nameList = propPariOut(name2Source, "knowledge.ttl", name2SourceParam);
        List<String> retList = new ArrayList<>();
        Map<String, String> retMap = clsOut(clsQuery, "knowledge.ttl");
        int count = 0;
        for(String nl : nameList) {
            count++;
            if(count % 10 == 0) {
                log.info("当前：" + count + " 总共：" + nameList.size());
            }
            if(StringUtils.isEmpty(nl) || nl.split(" ").length < 3) {
                continue;
            }
            String name = nl.split(" ")[0];
            String uri = nl.split(" ")[1];
            String pred = nl.split(" ")[2];
            String realPro2Source = String.format(pro2Source, uri, uri);
            List<String> sourceList = propPariOut(realPro2Source, "annotation.ttl", pro2SourceParam);
            for(String sl : sourceList) {
                if(StringUtils.isEmpty(sl) || sl.split(" ").length < 2) {
                    continue;
                }
                String proSource = sl.split(" ")[1];
                String predName = sl.split(" ")[0];
                if(!predName.equals(pred) && !pred.contains(predName)) {
                    continue;
                }
                String cls = retMap.get(uri);
                if(StringUtils.isEmpty(cls) || cls.length() < 2) {
                    continue;
                }
                fileWritter.write(name + " " + cls.substring(1, cls.length()) + " " + uri + " " + pred + " " + proSource + "\n");
                fileWritter.flush();
                retList.add(name + " " + uri + " " + pred + " " + proSource);
            }
        }
        fileWritter.close();
    }

    public void getRelationLoc(String preType, String outputPath) throws IOException {
        String name2Source = "SELECT ?name ?source ?pred ?obsource\n" +
                " WHERE\n" +
                "{ ?uri " + preType + " ?oburi.\n" +
                " ?uri <http://www.w3.org/2000/01/rdf-schema#label> ?name." +
                " ?oburi <http://www.w3.org/2000/01/rdf-schema#label> ?pred." +
                " ?uri <http://edukb.org/knowledge/0.1/property/common#source> ?source." +
                " ?oburi <http://edukb.org/knowledge/0.1/property/common#source> ?obsource.}";
        List<String> name2SourceParam = Arrays.asList("name", "source", "pred", "obsource");

        String pro2Source = "SELECT ?name ?source ?name\n" +
                " WHERE\n" +
                "{ <%s> " + preType + " ?source.\n" +
                " ?source <http://www.w3.org/2000/01/rdf-schema#label> ?name.}";
        List<String> pro2SourceParam = Arrays.asList("name", "source");

        String clsQuery = "SELECT ?source ?cls\n" +
                " WHERE\n" +
                "{ ?uri <http://edukb.org/knowledge/0.1/property/common#source> ?source." +
                " ?uri a ?cls.}";

        File file = new File(outputPath);
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        List<String> nameList = propPariOut(name2Source, "knowledge.ttl", name2SourceParam);
        List<String> retList = new ArrayList<>();
        Map<String, String> retMap = clsOut(clsQuery, "knowledge.ttl");
        int count = 0;
        for(String nl : nameList) {
            count++;
            if(StringUtils.isEmpty(nl) || nl.split(" ").length < 4) {
                continue;
            }
            if(count % 10 == 0) {
                log.info("文件：" + outputPath + " 当前：" + count + " 总共：" + nameList.size());
            }
            String name = nl.split(" ")[0];
            String uri = nl.split(" ")[1];
            String pred = nl.split(" ")[2];
            String oburi = nl.split(" ")[3];
            String realPro2Source = String.format(pro2Source, uri, uri);
            List<String> sourceList = propPariOut(realPro2Source, "annotation.ttl", pro2SourceParam);
            for(String sl : sourceList) {
                if(StringUtils.isEmpty(sl) || sl.split(" ").length < 2) {
                    continue;
                }
                String proSource = sl.split(" ")[1];
                String predName = sl.split(" ")[0];
                if(!predName.equals(pred) && !pred.contains(predName)) {
                    continue;
                }
                if(proSource.equals(oburi)) {
                    String cls = retMap.get(uri);
                    String objCls = retMap.get(oburi);
                    if(StringUtils.isEmpty(cls) || cls.length() < 2) {
                        continue;
                    }
                    fileWritter.write(name + " " + cls.substring(1, cls.length()) + " " + uri + " " + pred + " " + objCls + " " + proSource + "\n");
                    fileWritter.flush();
                    retList.add(name + " " + uri + " " + pred + " " + proSource);
                }
            }
        }
        fileWritter.close();
    }

    public List<String> propPariOut(String sql, String inputPath, List<String> propList) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        List<String> retList = new ArrayList<>();
        //ttl文件路径
        model.read(inputPath);
        Query query = QueryFactory.create(sql);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution querySolution = results.next();
            String ret = "";
            for(String prop : propList) {
                RDFNode node = querySolution.get(prop);
                String x = "";
                if(node != null) {
                    x = node.toString();
                }
                ret += " " + x.replace(" ", "");
            }
            retList.add(ret.trim());
        }
        return retList;
    }

    public Map<String, String> clsOut(String sql, String inputPath) {
        Model model = ModelFactory.createDefaultModel();
        Map<String, String> retMap = new HashMap<>();
        //ttl文件路径
        model.read(inputPath);
        Query query = QueryFactory.create(sql);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        StringBuilder sb = new StringBuilder();
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution querySolution = results.next();
            RDFNode nodeX = querySolution.get("source");
            RDFNode nodeY = querySolution.get("cls");
            String x = "";
            String y = "";
            if(nodeX != null) {
                x = nodeX.toString();
            }
            if(nodeY != null) {
                y = nodeY.toString();
            }
            retMap.putIfAbsent(x, "");
            String name = findClsName(y, model);
            if(!name.equals("")) {
                retMap.put(x, retMap.get(x) + "," + name);
            }

        }
        return retMap;
    }

    public String findClsName(String raw, Model model) {
        String clsNameFind = "SELECT ?name\n" +
                " WHERE\n" +
                "{ <%s> <http://www.w3.org/2000/01/rdf-schema#label> ?name}";
        clsNameFind = String.format(clsNameFind, raw);
        Query query = QueryFactory.create(clsNameFind);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        String ret = "";
        while (results.hasNext()) {
            QuerySolution querySolution = results.next();
            RDFNode nodeX = querySolution.get("name");
            String x = "";
            if(nodeX != null) {
                x = nodeX.toString();
            }
            ret = x;
        }
        return ret;
    }

    public void testSparql(String sql) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        //ttl文件路径
        model.read(inputPath);
        Query query = QueryFactory.create(sql);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        StringBuilder sb = new StringBuilder();
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution querySolution = results.next();
            RDFNode nodeX = querySolution.get("x");
            RDFNode nodeY = querySolution.get("y");
            RDFNode nodeZ = querySolution.get("z");
            String x = "";
            String y = "";
            String z = "";
            if(nodeX != null) {
                x = nodeX.toString();
            }
            if(nodeY != null) {
                y = nodeY.toString();
            }
            if(nodeZ != null) {
                z = nodeZ.toString();
            }
            System.out.println(x + " " + y + " " + z);
        }
    }

    public void writeFile(String sql) throws IOException {
        File file = new File(outputPath);
        FileWriter fileWritter = new FileWriter(file.getName(),true);
        Model model = ModelFactory.createDefaultModel();
        //ttl文件路径
        model.read(inputPath);
        Query query = QueryFactory.create(sql);
        QueryExecution qe =  QueryExecutionFactory.create(query, model);
        StringBuilder sb = new StringBuilder();
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution querySolution = results.next();
            RDFNode nodeX = querySolution.get("name");
            RDFNode nodeY = querySolution.get("source");
            String x = "";
            String y = "";
            if(nodeX != null) {
                x = nodeX.toString();
            }
            if(nodeY != null) {
                y = nodeY.toString();
            }
            sb.append(x + " " + y + "\n");
            fileWritter.write(sb.toString());
            fileWritter.flush();
            sb.delete(0, sb.length());
        }
        fileWritter.close();
    }
}
