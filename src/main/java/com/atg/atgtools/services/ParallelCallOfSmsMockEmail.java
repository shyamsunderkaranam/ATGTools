package com.atg.atgtools.services;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ParallelCallOfSmsMockEmail {

    @Autowired
    SmsFeatureToggleCheck smsFeatureToggleCheck;

    @Autowired
    MockCheckService mockCheckService;

    @Autowired
    EmailDummyModeCheckService emailDummyModeCheckService;

    @Autowired
    PrepareATGLinksService prepareATGLinksService;


    public JSONObject parallelCall(){


        List<Object> pList = new ArrayList(Arrays.asList(smsFeatureToggleCheck, mockCheckService,emailDummyModeCheckService));
        List<JSONObject> atgLinks = prepareATGLinksService.getAllATGEnvUrls("All");

        JSONObject tempJSONObject = new JSONObject();



        List<CompletableFuture<JSONObject>> futures = pList.stream()
                .map(obj -> {
                    return sharedCall(obj,atgLinks);
                })
                .collect(Collectors.toList());
        List<JSONObject> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                ;


        result.forEach(res ->
        {
            tempJSONObject.putAll(res);
        });



/*
        tempJSONObject.put("SmsFeatureToggleValues",smsFeatureToggleCheck.getData(atgLinks));
        tempJSONObject.put("MockValues",mockCheckService.getData(atgLinks));
        tempJSONObject.put("EmailDummyModeValues",emailDummyModeCheckService.getData(atgLinks));

 */
        return tempJSONObject;



    }

    public CompletableFuture<JSONObject> sharedCall(Object obj, List<JSONObject> atgLinks ){

        CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(new Supplier<JSONObject>() {
            @Override
            public JSONObject get() {
                if (obj instanceof SmsFeatureToggleCheck) {
                    SmsFeatureToggleCheck tmpObj = (SmsFeatureToggleCheck) obj;
                    JSONObject tmp = new JSONObject();
                    tmp.put("SmsFeatureToggleValues", tmpObj.getData(atgLinks));

                    return tmp;
                } else if (obj instanceof MockCheckService) {
                    MockCheckService tmpObj = (MockCheckService) obj;
                    JSONObject tmp = new JSONObject();
                    tmp.put("MockValues", tmpObj.getData(atgLinks));
                    return tmp;
                } else if (obj instanceof EmailDummyModeCheckService) {
                    EmailDummyModeCheckService tmpObj = (EmailDummyModeCheckService) obj;
                    JSONObject tmp = new JSONObject();
                    tmp.put("EmailDummyModeValues", tmpObj.getData(atgLinks));
                    return tmp;
                }
                return null;
            }

        });
        return future;

    }
}
