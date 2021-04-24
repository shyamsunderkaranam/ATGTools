package com.atg.atgtools.services;

import com.atg.atgtools.conf.EnvDataDAO;
import com.squareup.okhttp.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
public class EnableDisableIncrIndexing {

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    private String environment;
    private String enabled;

    Logger logger = LoggerFactory.getLogger(EnableDisableIncrIndexing.class);
    private final String URL_PRE_STRING = "http://atg-";
    private static final String ATG_IDX_CONFIG_FILE="config/monitoringConf.json";
    @Autowired
    private EnvDataDAO envData;

    public List<JSONObject> toggleEnableDisableIncrIndexing(String env, String enabledFlag){

        setEnvironment(env);
        setEnabled(enabledFlag);

        List<JSONObject> incIdexingDetailArray= new ArrayList<JSONObject>() ;
        JSONObject incIdexingDetail = new JSONObject();

        JSONArray config = envData.getData(ATG_IDX_CONFIG_FILE);
        incIdexingDetailArray =  (List<JSONObject>) ((JSONObject)config.get(0)).get("incrIdxConfig");


        List<CompletableFuture<JSONObject>> futures =
                incIdexingDetailArray.stream()
                        .map(this::toggleEnableDisableIncrIndexing)
                        .collect(Collectors.toList());

        List<JSONObject> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        logger.info("End Time: "+ LocalDateTime.now());

        return result;



    }

    private CompletableFuture<JSONObject> toggleEnableDisableIncrIndexing(JSONObject incIdexingDetail){


        CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(new Supplier<JSONObject>() {
            @Override
            public JSONObject get() {

                JSONObject indexingDetail = new JSONObject(incIdexingDetail);
                String idxUrl = URL_PRE_STRING + getEnvironment() + indexingDetail.get("postLink");
                indexingDetail.put("link",idxUrl) ;
                indexingDetail.put("enabled",getEnabled()) ;

                String enabled = indexingDetail.get("enabled").toString();
                logger.info("Link is "+idxUrl+" and Enable mode is "+enabled);
                Response response = null;
                try {

                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody body = RequestBody.create(mediaType, "newValue=" + enabled + "&submit=change");
                    Request request = new Request.Builder()
                            .url(idxUrl)
                            .method("POST", body)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .build();
                    response = client.newCall(request).execute();

                    Document doc = Jsoup.connect(idxUrl).get();


                    Elements content = doc.getElementsByAttributeValueMatching("style", "white-space:pre") ;

                    logger.info(Thread.currentThread().getName() + " For url "+idxUrl+" Value: "+content.html());

                    indexingDetail.remove("postLink");
                    if(response.isSuccessful() && content.html().toString().equalsIgnoreCase(indexingDetail.get("enabled").toString())) {
                        indexingDetail.put("state", "DONE");

                    }
                    else
                        indexingDetail.put("state","NOT_DONE");
                    return indexingDetail;
                } catch (Exception e) {

                    e.printStackTrace();
                    indexingDetail.put("state","ERROR_CONNECTING");
                    return indexingDetail;
                }
            }
        });
        return future;
    }

    /*public static void main(String[] args) {
        EnableDisableIncrIndexing enableDisableIncrIndexing = new EnableDisableIncrIndexing();
        System.out.println(enableDisableIncrIndexing.toggleEnableDisableIncrIndexing("Something","false"));
    }*/

}
