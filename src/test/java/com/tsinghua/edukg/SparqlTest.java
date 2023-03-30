package com.tsinghua.edukg;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

@SpringBootTest
@Slf4j
public class SparqlTest {
    /**
     * 1: 打印
     * 2: 写入文件
     */
    int threadShold = 2;

    /**
     * ttl文件路径
     */
    String inputPath = "./chinese_版本1.ttl";

    /**
     * 输出txt文件路径
     */
    String outputPath = "./storyOut.txt";

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

        switch (threadShold) {
            case 1:
                testSparql(story);
                break;
            case 2:
                writeFile(story);
                break;
            default:
                break;
        }
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
            RDFNode nodeX = querySolution.get("xn");
            RDFNode nodeY = querySolution.get("yn");
            String x = "";
            String y = "";
            if(nodeX != null) {
                x = nodeX.toString();
            }
            if(nodeY != null) {
                y = nodeY.toString();
            }
            System.out.println(x + " " + y);
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
            RDFNode nodeX = querySolution.get("xn");
            RDFNode nodeY = querySolution.get("yn");
            String x = "";
            String y = "";
            if(nodeX != null) {
                x = nodeX.toString();
            }
            if(nodeY != null) {
                y = nodeY.toString();
            }
            sb.append(x + " " + y + "\n");
        }
        fileWritter.write(sb.toString());
        fileWritter.close();
    }
}
