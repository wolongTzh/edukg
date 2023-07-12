package com.tsinghua.edukg.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
@Slf4j
public class SparqlUtil {

    public static List<String> mainProcess(String predicate, int type) throws IOException {
        String code = findCode(predicate);
        List<String> retList = new ArrayList<>();
        if(StringUtils.isEmpty(code)) {
            return retList;
        }
        code = "<" + code + ">";
        if(type == 0) {
            retList = getProLoc(code);
        }
        else {
            retList = getRelationLoc(code);
        }
        return retList;
    }

    public static List<String> getProLoc(String preType) throws IOException {
        List<String> retList = new ArrayList<>();
        int maxSize = 10;
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

        List<String> nameList = propPariOut(name2Source, "knowledge.ttl", name2SourceParam);
        Set<String> nameCount = new HashSet<>();
        for(String nl : nameList) {
            if(retList.size() >= maxSize) {
                break;
            }
            if(StringUtils.isEmpty(nl) || nl.split(" ").length < 3) {
                continue;
            }
            String name = nl.split(" ")[0];
            if(nameCount.contains(name)) {
                continue;
            }
            else {
                nameCount.add(name);
            }
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
                retList.add(name + " " + uri + " " + pred + " " + proSource);
            }
        }
        return retList;
    }

    public static List<String> getRelationLoc(String preType) throws IOException {
        int maxSize = 10;
        List<String> retList = new ArrayList<>();
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

        List<String> nameList = propPariOut(name2Source, "knowledge.ttl", name2SourceParam);
        Set<String> nameCount = new HashSet<>();
        for(String nl : nameList) {
            if(retList.size() >= maxSize) {
                break;
            }
            if(StringUtils.isEmpty(nl) || nl.split(" ").length < 4) {
                continue;
            }
            String name = nl.split(" ")[0];
            if(nameCount.contains(name)) {
                continue;
            }
            else {
                nameCount.add(name);
            }
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
                    retList.add(name + " " + uri + " " + pred + " " + proSource);
                }
            }
        }
        return retList;
    }

    public static String findCode(String predicate) {
        String inputPath = "./knowledge.ttl";
        String findCodeSql = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix co: <http://edukb.org/knowledge/0.1/property/common#>\n" +
                " SELECT ?uri \n" +
                " WHERE\n" +
                "{ ?uri rdfs:label \"%s\".}";
        findCodeSql = String.format(findCodeSql, predicate);
        Model model = ModelFactory.createDefaultModel();
        //ttl文件路径
        model.read(inputPath);
        Query query = QueryFactory.create(findCodeSql);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        Map<String, List<String>> sourceMap = new HashMap<>();
        String x = "";
        while (results.hasNext()) {
            QuerySolution querySolution = results.next();
            RDFNode nodeX = querySolution.get("uri");
            if(nodeX != null) {
                x = nodeX.toString();
            }
        }
        return x;
    }

    public static List<String> propPariOut(String sql, String inputPath, List<String> propList) throws IOException {
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
            boolean vanishFlag = false;
            for(String prop : propList) {
                RDFNode node = querySolution.get(prop);
                String x = "";
                if(node != null) {
                    x = node.toString();
                }
                if(prop.equals("source") && !x.contains("http")) {
                    vanishFlag = true;
                }
                ret += " " + x.replace(" ", "");
            }
            if(!vanishFlag) {
                retList.add(ret.trim());
            }
        }
        return retList;
    }
}
