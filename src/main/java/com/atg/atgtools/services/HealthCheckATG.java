package com.atg.atgtools.services;

import com.atg.atgtools.conf.EnvDataDAO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class HealthCheckATG {

    Logger logger = LoggerFactory.getLogger(HealthCheckATG.class);
    private JSONObject envJSONObject, personaJSONObject;
    private static final String ATG_ENV_STATS_FILE = "EnvData/Atg_Env_State.json";
    private static final String ATG_ENV_STATS_ERR_FILE = "EnvData/Atg_Env_Err_State.json";
    private static final String ATG_ENV_CONFIG_FILE="config/tier_envs.json";
    private static final int CONN_TIME_OUT = 3000;
    private EnvDataDAO envData;
    private String tierName;
    List<JSONObject> agtSilos,backOfc,stfSilos,genLinks;
    @Autowired
    PrepareATGLinksService prepareATGLinksService;


    public List<JSONObject> getAllATGEnvUrls(String tierNames) {

        logger.info(Thread.currentThread().getName()+" Preparing the ATG environment links now");
        List<JSONObject> envLinks = prepareATGLinksService.getAllATGEnvUrls("All");
        logger.info(Thread.currentThread().getName() + " Now checking all ATG URLs health");
        List<CompletableFuture<JSONObject>> futures =
                envLinks.stream()
                        .map(this::getResponseCodeForURLAsync)
                        .collect(Collectors.toList());

        List<JSONObject> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        logger.info("End Time: "+ LocalDateTime.now());
        return result;
    }

    @Async
    public List<JSONObject> getURLAvailability (List<JSONObject> urlList ){


        List<JSONObject> tempList = urlList
                //.parallelStream()
                .parallelStream()
                .map(this::getResponseCodeForURL)
                //.parallel()
                .collect(Collectors.toList());


        return tempList;

     }
    public JSONObject getResponseCodeForURL(JSONObject envJSONObject)  {
        int responsecode = 0 ;
        JSONObject env = envJSONObject;
        try {

            logger.info(Thread.currentThread().getName()+" For  "+env);
            final URL url = new URL(envJSONObject.get("Link").toString());
            String requestMethod = "HEAD";
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod(requestMethod);

            huc.setConnectTimeout(CONN_TIME_OUT);
            huc.setReadTimeout(CONN_TIME_OUT);

            huc.connect();
            responsecode = huc.getResponseCode();


        }catch(Exception e) {
            //e.printStackTrace();
        }
        env.put("State", responsecode);
        return env;
    }

    public CompletableFuture<JSONObject> getResponseCodeForURLAsync(JSONObject envJSONObject)  {

        CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(new Supplier<JSONObject>() {
            @Override
            public JSONObject get() {
                int responsecode = 0;
                JSONObject env = envJSONObject;
                try {

                    logger.info(Thread.currentThread().getName() + " For  " + env);
                    final URL url = new URL(envJSONObject.get("Link").toString());
                    String requestMethod = "HEAD";
                    HttpURLConnection.setFollowRedirects(true);
                    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                    huc.setRequestMethod(requestMethod);

                    huc.setConnectTimeout(CONN_TIME_OUT);
                    huc.setReadTimeout(CONN_TIME_OUT);

                    huc.connect();
                    responsecode = huc.getResponseCode();


                } catch (Exception e) {
                    //e.printStackTrace();
                }
                env.put("State", responsecode);
                return env;
            }
            });

        return future;
    }
}
