package com.tsinghua.edukg.tools;

import com.tsinghua.edukg.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
public class TextBookSlice2ES {
    @Test
    public void readDir() throws IOException {
        String rootPath = "";
        String basePath = "./out%s.txt";
        int acc = 0;
        int num = 1;
        File out = new File(String.format(basePath, num));
        FileWriter fileWriter = new FileWriter(out.getName());
        File rootList = new File(rootPath);
        for(String fileName : rootList.list()) {
            File midList = new File(rootPath + "/" + fileName);
            String childPath = "";
            if(midList.list().length > 3) {
                childPath = rootPath + "/" + fileName + "/Text";
            }
            else {
                childPath = rootPath + "/" + fileName + "/OEBPS/Text";
            }
            File childList = new File(childPath);
            for(String midName : childList.list()) {
                String path = childPath + "/" + midName;
                List<String> retList = readHtml(path);
                for(String ret : retList) {
                    fileWriter.write(ret + "\n");
                    fileWriter.flush();
                }
                acc += retList.size();
                if(acc > 10000) {
                    acc = 0;
                    num++;
                    out = new File(String.format(basePath, num));
                    fileWriter = new FileWriter(out.getName());
                }
            }
        }
        fileWriter.close();
    }

    public List<String> readHtml(String path) {
        File file = new File(path);
        List<String> retList = new ArrayList<>();
        List<String> contentList = CommonUtil.readTextFromPath(path);
        for(String content : contentList) {
            String analyseResult = analyseOneLine(content);
            if(blankJudge(analyseResult)) {
                continue;
            }
            retList.add(analyseResult);
        }
        return retList;
    }

    public String analyseOneLine(String line) {
        boolean ignoreFlag = false;
        String result = "";
        for(char c : line.toCharArray()) {
            if(c == '<') {
                ignoreFlag = true;
            }
            else if(c == '>') {
                ignoreFlag = false;
            }
            else {
                if(!ignoreFlag) {
                    result += c;
                }
            }
        }
        result = result.replace(" ", "");
        result = result.replace("\n", "");
        return result;
    }

    public boolean blankJudge(String content) {
        if(content.length() < 50) {
            return true;
        }
        boolean blankJudge = true;
        for(char c : content.toCharArray()) {
            if(c != ' ' && c != '\n') {
                blankJudge = false;
            }
        }
        return blankJudge;
    }
}
