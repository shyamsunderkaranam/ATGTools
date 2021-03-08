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

    private static final int CONN_TIME_OUT = 3000;
    @Autowired
    PrepareATGLinksService prepareATGLinksService;


    public List<JSONObject> getAllATGEnvUrls(String tierNames) {

        logger.info(Thread.currentThread().getName()+" Preparing the ATG environment links now");
        List<JSONObject> envLinks = prepareATGLinksService.getAllATGEnvUrls(tierNames);
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
