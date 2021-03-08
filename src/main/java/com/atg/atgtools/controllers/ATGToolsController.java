package com.atg.atgtools.controllers;

import com.atg.atgtools.services.HealthCheckATG;
import com.atg.atgtools.services.MockCheckService;
import com.atg.atgtools.services.SmsFeatureToggleCheck;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.time.StopWatch;

@RestController
public class ATGToolsController {

	Logger logger = LoggerFactory.getLogger(ATGToolsController.class);
	@Autowired
	HealthCheckATG healthCheckATG;

	@Autowired
	SmsFeatureToggleCheck smsFeatureToggleCheck;

	@Autowired
	MockCheckService mockCheckService;

	StopWatch stopWatch= StopWatch.create();

	@RequestMapping("/jaiganesh")
	public ResponseEntity<String> testMethod(){

		return ResponseEntity.ok("JAI GANESH");
	}

	@RequestMapping("/atgEnvStats")
	public List<JSONObject> getATGEnvAvailability() {
		logger.info("Before the getATGEnvsStatsJSON call");
		logger.info("Start Time: "+ LocalDateTime.now());
		stopWatch.start();
		List<JSONObject> tempList=  healthCheckATG.getAllATGEnvUrls("all");
		stopWatch.stop();
		logger.info("Total time taken: "+stopWatch.toString());
		stopWatch.reset();
		return tempList;
	}

	@RequestMapping("/atgSMSFTValues")
	public List<JSONObject> getATGSMSFTValues() {
		logger.info("Before the generateSMSFeatureToggleValues call");
		logger.info("Start Time: "+ LocalDateTime.now());
		stopWatch.start();
		List<JSONObject> tempList=  smsFeatureToggleCheck.generateSMSFeatureToggleValues("all");
		stopWatch.stop();
		logger.info("Total time taken: "+stopWatch.toString());
		stopWatch.reset();
		return tempList;
	}

	@RequestMapping("/atgMockValues")
	public List<JSONObject> getMockValues() {
		logger.info("Before the getMockValues call");
		stopWatch.start();
		logger.info("Start Time: "+ LocalDateTime.now());
		List<JSONObject> tempList= mockCheckService.getMockValues("all");
		stopWatch.stop();
		logger.info("Total time taken: "+stopWatch.toString());
		stopWatch.reset();
		return tempList;
	}
}
