package com.atg.atgtools.services;

import com.atg.atgtools.controllers.ATGToolsController;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class MockCheckService {
    // Google search: how to call multiple rest api parallel in spring boot
    //https://github.com/thombergs/code-examples/blob/master/spring-boot/spring-boot-logging-2/src/main/resources/application.properties
    Logger logger = LoggerFactory.getLogger(MockCheckService.class);
    public void checkMock(){
        Document doc;
        try {
            String url="http://atg-pvtbquk-ndc-app02.ghanp.kfplc.com:8030/dyn/admin/nucleus//kf/commerce/mocks/MockConfiguration/";
            doc = Jsoup.connect(url).get();
            Element table = doc.select("table").get(0); //select the first table.
            Elements rows = table.select("tr");

            for (int i = 14; i < rows.size(); i++) { //first row is the col names so skip it.
                Element row = rows.get(i);
                Elements cols = row.select("td");
                String mockProperty, mockValue;
                mockProperty = cols.get(0).select("a").text() ;
                mockValue = cols.get(1).select("span").text() ;
                if (mockProperty.contains("mock"))
                    logger.info("{} : {}",mockProperty,mockValue);
                    //System.out.println(mockProperty+" : "+mockValue);

            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //return "error";
        }
    }

    public static void main(String[] args) {
        MockCheckService m = new MockCheckService();
        m.checkMock();
    }
}
