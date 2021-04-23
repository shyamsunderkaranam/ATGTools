package com.atg.atgtools.services;

import com.squareup.okhttp.*;
import org.springframework.stereotype.Service;

@Service
public class EnableDisableIncrIndexing {

    public Response toggleEnableDisableIncrIndexing(String environmentName){
        Response response = null;
        try{

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "newValue=false&submit=change");
            Request request = new Request.Builder()
                    .url("http://atg-pvtcapl-ndc-aux01.ghanp.kfplc.com:8050/dyn/admin/nucleus/atg/userprofiling/textsearch/schedule/ProfileIncrementalScheduleConfig/?propertyName=enabled")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            response = client.newCall(request).execute();
            return response;
        } catch (Exception e) {

            e.printStackTrace();
            return  response;
        }
    }

    public static void main(String[] args) {
        EnableDisableIncrIndexing enableDisableIncrIndexing = new EnableDisableIncrIndexing();
        System.out.println(enableDisableIncrIndexing.toggleEnableDisableIncrIndexing("Something"));
    }

}
