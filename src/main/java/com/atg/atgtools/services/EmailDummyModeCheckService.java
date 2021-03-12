package com.atg.atgtools.services;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Service
public class EmailDummyModeCheckService {
    private final String EMAIL_DUMMY_MODE_PROP = "/nucleus/atg/dynamo/service/SMTPEmail/?propertyName=dummyMode";
    @Autowired
    PrepareATGLinksService prepareATGLinksService;
    Logger logger = LoggerFactory.getLogger(EmailDummyModeCheckService.class);

    public CompletableFuture<JSONObject> getEmailDummyModeValue(JSONObject envJSONObject) {

        CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(new Supplier<JSONObject>() {
            @Override
            public JSONObject get() {
                String url = envJSONObject.get("Link").toString().concat(EMAIL_DUMMY_MODE_PROP) ;
                JSONObject recordState = new JSONObject(envJSONObject);
                recordState.put("Link",url);

                Document doc;
                try {
                    doc = Jsoup.connect(url).get();


                    Elements content = doc.getElementsByAttributeValueMatching("style", "white-space:pre") ;
                    logger.info(Thread.currentThread().getName() + " For url "+url+" Value: "+content.html());
                    recordState.put("DummyModeValue",content.html());
                    recordState.remove("State");


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    logger.info(Thread.currentThread().getName() + " For url "+url+" Value: Unable to connect");
                    //e.printStackTrace();
                    recordState.put("DummyModeValue","error");
                    recordState.remove("State");

                }
                return recordState;
            }
        });

        return future;
    }
	/*
	public static void main(String[] args) {
		LocalDateTime startTime = LocalDateTime.now();
		SmsFeatureToggleCheck smsCheck= new SmsFeatureToggleCheck();
		System.out.println(smsCheck.generateSMSFeatureToggleValues("All"));
		LocalDateTime endTime = LocalDateTime.now();
		System.out.println("Start time: "+startTime);
		System.out.println("End time: "+endTime);

	}

	 */

    public List<JSONObject> generateEmailDummyModeValues(String tierNames) {

        logger.info(Thread.currentThread().getName()+" Preparing the ATG environment links now");
        List<JSONObject> envLinks = prepareATGLinksService.getAllATGEnvUrls(tierNames);
        logger.info(Thread.currentThread().getName() + " Now checking all ATG URLs health");
        List<CompletableFuture<JSONObject>> futures =
                envLinks.stream()
                        .filter(envObj -> envObj.get("Link").toString().contains("/dyn/admin"))
                        .map(this::getEmailDummyModeValue)
                        .collect(Collectors.toList());

        List<JSONObject> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        logger.info("End Time: "+ LocalDateTime.now());
        return result;
    }

    public List<JSONObject> generateEmailDummyModeValues(List<JSONObject> envLinks) {
        logger.info(Thread.currentThread().getName() + " Now checking all ATG URLs health");
        List<CompletableFuture<JSONObject>> futures =
                envLinks.stream()
                        .filter(envObj -> envObj.get("Link").toString().contains("/dyn/admin"))
                        .map(this::getEmailDummyModeValue)
                        .collect(Collectors.toList());

        List<JSONObject> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
            logger.info("End Time: "+ LocalDateTime.now());
            return result;
    }
    public List<JSONObject> getData(List<JSONObject> envLinks){

        return generateEmailDummyModeValues(envLinks);
    }
}
