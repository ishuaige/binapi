package com.niuma.binapi.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取 <a>https://api.aa1.cn/</a>网站的接口，如有不合适，请联系，非常感谢
 * @author niuma
 * @create 2023-05-28 14:28
 */
@Component
public class Apiaa1DataSource implements InterfaceInfoDataSource<InterfaceInfo>{
    @Override
    public Page<InterfaceInfo> doSearch(String searchText, long pageNum, long pageSize) {
        Page<InterfaceInfo> interfaceInfoPage = null;
        try {
            String url = String.format("https://api.aa1.cn/v1/apilist?search=%s", searchText);
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select(".flink-list-item");
            List<InterfaceInfo> interfaceInfoList = new ArrayList<>();
            for (Element element : elements) {
                // 取url
                String href = element.select(".cf-friends-link").attr("href");
                Elements infoEle = element.select(".flink-item-info");
                String name = infoEle.select(".flink-item-name").text();
                String desc = infoEle.select(".flink-item-desc").text();;
                String interfaceDocUrl = "https://api.aa1.cn" + href;

                InterfaceInfo interfaceInfo = new InterfaceInfo();
                interfaceInfo.setName(name);
                interfaceInfo.setDescription(desc);
                interfaceInfo.setUrl(interfaceDocUrl);
                interfaceInfoList.add(interfaceInfo);
            }
            interfaceInfoPage = new Page<>(pageNum,pageSize);
            interfaceInfoPage.setRecords(interfaceInfoList);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询第三方失败！");
        }
        return interfaceInfoPage;
    }
}
