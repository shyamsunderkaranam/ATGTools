package com.atg.atgtools.services;


import com.atg.atgtools.conf.EnvDataDAO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Service
public class MockCheckService {
    // Google search: how to call multiple rest api parallel in spring boot
    //https://github.com/thombergs/code-examples/blob/master/spring-boot/spring-boot-logging-2/src/main/resources/application.properties
    @Autowired
    PrepareATGLinksService prepareATGLinksService;

    private static final String ATG_ENV_CONFIG_FILE="config/tier_envs.json";
    @Autowired
    private EnvDataDAO envData;

    private JSONObject mockFilter;
    private List<String> mocksToInclude;
    private String allowAllMock;

    private final String MOCKURLPATH = "/nucleus//kf/commerce/mocks/MockConfiguration/";

    Logger logger = LoggerFactory.getLogger(MockCheckService.class);
    public CompletableFuture<JSONObject> getMockValueForATGEnv(JSONObject envDetails){

        CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(new Supplier<JSONObject>() {
        @Override
        public JSONObject get() {
            JSONObject mockJSONObject = new JSONObject(envDetails);
            String url = mockJSONObject.get("Link").toString();
            Document doc;
            try {
                //String url="http://atg-pvtbquk-ndc-app02.ghanp.kfplc.com:8030/dyn/admin/nucleus//kf/commerce/mocks/MockConfiguration/";
                url= url.concat(MOCKURLPATH);
                mockJSONObject.put("Link",url);
                doc = Jsoup.connect(url).get();
                String currentTier = mockJSONObject.get("tier").toString();
                Element table = doc.select("table").get(0);
                Elements rows = table.select("tr");
                JSONArray mockJSONArray = new JSONArray();


                for (int i = 14; i < rows.size(); i++) {
                    JSONObject mockJSONPropObject = new JSONObject();
                    Element row = rows.get(i);
                    Elements cols = row.select("td");
                    String mockProperty, mockValue;
                    mockProperty = cols.get(0).select("a").text();
                    mockValue = cols.get(1).select("span").text();
                    if (mockProperty.contains("mock") &&
                            (allowAllMock.equalsIgnoreCase("Y") ||
                                    mocksToInclude.contains(mockProperty)))
                    {
                        logger.info("Tier: {} URL: {} Mock Property {} : Mock Value {}", currentTier, url, mockProperty, mockValue);
                        mockJSONPropObject.put("mockKey", mockProperty);
                        mockJSONPropObject.put("mockValue", mockValue);
                        mockJSONPropObject.put("mockURL", url.concat("/?propertyName=").concat(mockProperty));
                        mockJSONArray.add(mockJSONPropObject);


                    } //if (mockProperty.contains("mock") &&

                    //System.out.println(mockProperty+" : "+mockValue);

                }

                mockJSONObject.put("State", 200);
                mockJSONObject.put("mocks",mockJSONArray);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                mockJSONObject.put("State", 500);
                mockJSONObject.put("mocks","ERROR");
                //e.printStackTrace();
                logger.info("Unable to connect OR URL Not reachable. URL: {} ", url);
                //return "error";
            }
            return mockJSONObject;
        }

    });
        return future;
    }

    public List<JSONObject> getMockValues(String tierNames){
        logger.info(Thread.currentThread().getName()+" Preparing the ATG environment Mock URLs now for tier: "+tierNames);
        List<JSONObject> envLinks = new ArrayList<JSONObject>(prepareATGLinksService.getAllATGEnvUrls(tierNames));
        mockFilter = new JSONObject();
        mocksToInclude = new ArrayList<>();
        JSONArray config = envData.getData(ATG_ENV_CONFIG_FILE);
        mockFilter = (JSONObject) config.get(2);
        mocksToInclude = (List<String>)mockFilter.get("mocksToBeIncluded");
        allowAllMock = mockFilter.get("allowAllMocks").toString();

        logger.info(Thread.currentThread().getName() + " Currently these mocks are configured : "+mocksToInclude);
        logger.info(Thread.currentThread().getName() + " Currently All mocks are allowed is : "+allowAllMock);
        logger.info(Thread.currentThread().getName() + " Now checking all ATG Environments Mock");
        logger.info(Thread.currentThread().getName() + "And the environment links are "+ envLinks);
        List<CompletableFuture<JSONObject>> futures =
                envLinks.stream()
                        .filter(envObj -> envObj.get("Link").toString().contains("/dyn/admin") &&
                                envObj.get("Link").toString().contains("app01") &&
                                envObj.get("Link").toString().contains("8030"))
                        .map(this::getMockValueForATGEnv)
                        .collect(Collectors.toList());

        List<JSONObject> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        logger.info("End Time: "+ LocalDateTime.now());

        return result;
    }

    public List<JSONObject> getMockValues(List<JSONObject> envLinks){

        logger.info(Thread.currentThread().getName() + " Now checking all ATG Environments Mock");
        mockFilter = new JSONObject();
        mocksToInclude = new ArrayList<>();
        JSONArray config = envData.getData(ATG_ENV_CONFIG_FILE);
        mockFilter = (JSONObject) config.get(2);
        mocksToInclude = (List<String>)mockFilter.get("mocksToBeIncluded");
        allowAllMock = mockFilter.get("allowAllMocks").toString();

        logger.info(Thread.currentThread().getName() + " Currently these mocks are configured : "+mocksToInclude);
        logger.info(Thread.currentThread().getName() + " Currently All mocks are allowed is : "+allowAllMock);
        logger.info(Thread.currentThread().getName() + " Now checking all ATG Environments Mock");
        List<CompletableFuture<JSONObject>> futures =
                envLinks.stream()
                        .filter(envObj -> envObj.get("Link").toString().contains("/dyn/admin") &&
                                envObj.get("Link").toString().contains("app01") &&
                                envObj.get("Link").toString().contains("8030"))
                        .map(this::getMockValueForATGEnv)
                        .collect(Collectors.toList());

        List<JSONObject> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        logger.info("End Time: "+ LocalDateTime.now());

        return result;
    }
    public List<JSONObject> getData(List<JSONObject> envLinks){

        return getMockValues(envLinks);
    }
}
