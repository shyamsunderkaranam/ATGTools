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


    //public List<JSONObject> parallelCall(){
    public JSONObject parallelCall(){


        List<Object> pList = new ArrayList(Arrays.asList(smsFeatureToggleCheck, mockCheckService,emailDummyModeCheckService));
        List<JSONObject> atgLinks = prepareATGLinksService.getAllATGEnvUrls("All");
        //List<List<JSONObject>> tempList
        //List<JSONObject> tempJSONObject
        JSONObject tempJSONObject = new JSONObject();
        tempJSONObject.put("SmsFeatureToggleValues",smsFeatureToggleCheck.getData(atgLinks));
        tempJSONObject.put("MockValues",mockCheckService.getData(atgLinks));
        tempJSONObject.put("EmailDummyModeValues",emailDummyModeCheckService.getData(atgLinks));
/*
                pList.stream()
        .map(obj ->
        {

            if(obj instanceof SmsFeatureToggleCheck) {
                SmsFeatureToggleCheck tmpObj = (SmsFeatureToggleCheck) obj;
                //JSONObject tmp = new JSONObject();
                //tmp.put("SmsFeatureToggleValues",tmpObj.getData(atgLinks));
                tempJSONObject.put("SmsFeatureToggleValues",tmpObj.getData(atgLinks));
                //return tmp;
            }
            else if(obj instanceof MockCheckService){
                MockCheckService tmpObj = (MockCheckService) obj;
                //JSONObject tmp = new JSONObject();
                tempJSONObject.put("MockValues",tmpObj.getData(atgLinks));
                //return tmp;
            }
            else if(obj instanceof EmailDummyModeCheckService){
                EmailDummyModeCheckService tmpObj = (EmailDummyModeCheckService) obj;
                //JSONObject tmp = new JSONObject();
                tempJSONObject.put("EmailDummyModeValues",tmpObj.getData(atgLinks));
                //return tmp;
            }

            return null;

        })
                        ;

 */
               // .collect(Collectors.toList());
        /*
        List<JSONObject> tempJSONObject = new ArrayList<JSONObject>();
        tempList.stream()
                .forEach(tmpList -> {
                    tempJSONObject.addAll(tmpList);
                });*/



        return tempJSONObject;



    }
}
