package com.niuma.binapi.service;


import com.niuma.binapicommon.model.entity.InterfaceInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author niuma
 * @create 2023-03-17 9:17
 */
@SpringBootTest
public class CrawlerTest {


    public static void main(String[] args) throws IOException {
        testFetchPicture();
    }


    public static void testFetchPicture() throws IOException {
        String keyword = "图片";
        String url = String.format("https://api.aa1.cn/v1/apilist?search=%s", keyword);
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".flink-list-item");
        List<InterfaceInfo> interfaceInfoList = new ArrayList<>();
        for (Element element : elements) {
            // 取url
            String href = element.select(".cf-friends-link").attr("href");
            Elements infoEle = element.select(".flink-item-info");
            String name = infoEle.select(".flink-item-name").text();
            String desc = infoEle.select(".flink-item-desc").text();;
            String interfaceDocUrl = "https://api.aa1.cn/doc" + href;

            InterfaceInfo interfaceInfo = new InterfaceInfo();
            interfaceInfo.setName(name);
            interfaceInfo.setDescription(desc);
            interfaceInfo.setUrl(interfaceDocUrl);
            interfaceInfoList.add(interfaceInfo);

        }
        System.out.println(interfaceInfoList);
    }


}
