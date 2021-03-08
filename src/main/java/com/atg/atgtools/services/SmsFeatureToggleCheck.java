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
public class SmsFeatureToggleCheck {

	private final String SMSFEATURETOGGLEPATH = "/nucleus/kf/common/featuretoggles/DefaultFeatureToggles/?propertyName=featureToggles.enableSMSService" ;

	@Autowired
	PrepareATGLinksService prepareATGLinksService;
	Logger logger = LoggerFactory.getLogger(SmsFeatureToggleCheck.class);
	public SmsFeatureToggleCheck() {
		
	}
	public CompletableFuture<JSONObject> getSMSFeatureToggleValue(JSONObject envJSONObject) {

		CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(new Supplier<JSONObject>() {
			@Override
			public JSONObject get() {
				String url = envJSONObject.get("Link").toString().concat(SMSFEATURETOGGLEPATH) ;
				JSONObject recordState = envJSONObject;

				Document doc;
				try {
					doc = Jsoup.connect(url).get();


					Elements content = doc.getElementsByAttributeValueMatching("style", "white-space:pre") ;
					logger.info(Thread.currentThread().getName() + " For url "+url+" Value: "+content.html());
					recordState.put("SMSFTValue",content.html());
					recordState.remove("State");


				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.info(Thread.currentThread().getName() + " For url "+url+" Value: Unable to connect");
					//e.printStackTrace();
					recordState.put("SMSFTValue","error");
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

	public List<JSONObject> generateSMSFeatureToggleValues(String tierNames) {

		logger.info(Thread.currentThread().getName()+" Preparing the ATG environment links now");
		List<JSONObject> envLinks = prepareATGLinksService.getAllATGEnvUrls(tierNames);
		logger.info(Thread.currentThread().getName() + " Now checking all ATG URLs health");
		List<CompletableFuture<JSONObject>> futures =
				envLinks.stream()
						.filter(envObj -> envObj.get("Link").toString().contains("/dyn/admin"))
						.map(this::getSMSFeatureToggleValue)
						.collect(Collectors.toList());

		List<JSONObject> result =
				futures.stream()
						.map(CompletableFuture::join)
						.collect(Collectors.toList());
		logger.info("End Time: "+ LocalDateTime.now());
		return result;
	}

}
