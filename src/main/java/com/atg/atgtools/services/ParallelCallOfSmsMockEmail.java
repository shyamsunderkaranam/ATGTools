package com.atg.atgtools.services;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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


    public List<JSONObject> parallelCall(){

        List<Object> pList = new ArrayList(Arrays.asList(smsFeatureToggleCheck, mockCheckService,emailDummyModeCheckService));

        List<List<JSONObject>> tempList = pList.stream()
        .map(obj ->
        {
            List<JSONObject> atgLinks = prepareATGLinksService.getAllATGEnvUrls("All");
            if(obj instanceof SmsFeatureToggleCheck) {
                SmsFeatureToggleCheck tmpObj = (SmsFeatureToggleCheck) obj;
                return tmpObj.getData(atgLinks);
            }
            else if(obj instanceof MockCheckService){
                SmsFeatureToggleCheck tmpObj = (SmsFeatureToggleCheck) obj;
                return tmpObj.getData(atgLinks);
            }
            else if(obj instanceof EmailDummyModeCheckService){
                SmsFeatureToggleCheck tmpObj = (SmsFeatureToggleCheck) obj;
                return tmpObj.getData(atgLinks);
            }

            return null;

        })
                .collect(Collectors.toList());
        List<JSONObject> tempJSONObject = new ArrayList<JSONObject>();
        tempList.stream()
                .forEach(tmpList -> {
                    tempJSONObject.addAll(tmpList);
                });
        return tempJSONObject;

    }
}
