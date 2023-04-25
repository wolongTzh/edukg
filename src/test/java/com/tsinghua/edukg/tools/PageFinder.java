package com.tsinghua.edukg.tools;

import com.tsinghua.edukg.utils.XpointerUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import com.alibaba.fastjson.JSON;

@SpringBootTest
@Slf4j
public class PageFinder {

    @Test
    public void findPageIndex() throws IOException {

        String sourceUrl = "http://kb.cs.tsinghua.edu.cn/apibztask/label/242#xpointer(start-point(string-range(//BODY/TABLE[2]/TBODY[1]/TR[7]/TD[1]/text()[1],'',0))/range-to(string-range(//BODY/TABLE[2]/TBODY[1]/TR[7]/TD[1]/text()[1],'',2)))";
        String htmlPath = "./Chapter_03_第三部分文学常识文化常识和常见名句名篇.html";
        String doc = fetchHtml(htmlPath);
        List<String> pager = XpointerUtil.getPager(doc, sourceUrl);
        System.out.println(JSON.toJSONString(pager));
    }

    public String fetchHtml(String path) throws IOException {
        File file = new File(path);
        String html = "";
        if(file.exists()){
            Long filelength = file.length(); // 获取文件长度
            byte[] filecontent = new byte[filelength.intValue()];
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
            html = new String(filecontent, "utf-8");// 返回文件内容,默认编码
        }
        return html;
    }
}
