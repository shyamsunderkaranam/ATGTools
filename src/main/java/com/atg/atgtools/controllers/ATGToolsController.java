package com.atg.atgtools.controllers;

import com.atg.atgtools.services.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

	@Autowired
	EmailDummyModeCheckService emailDummyModeCheckService;

	@Autowired
	ParallelCallOfSmsMockEmail parallelCallOfSmsMockEmail;

	@Autowired
	EnableDisableIncrIndexing enableDisableIncrIndexing;

	StopWatch stopWatch= StopWatch.create();

	@CrossOrigin(allowedHeaders = "Access-Control-Allow-Origin")
	@RequestMapping("/jaiganesh")
	public ResponseEntity<String> testMethod(){

		return ResponseEntity.ok("JAI GANESH");
	}

	@CrossOrigin(allowedHeaders = "Access-Control-Allow-Origin")
	@RequestMapping(value = {"/atgEnvStats","/atgEnvStats/{tierName}"}, method = RequestMethod.GET)
	public ResponseEntity<List<JSONObject>> getATGEnvAvailability(@PathVariable(value = "tierName",required = false) String tierName) {
		String tier = "All";
		if(tierName==null || tierName.equals(null) || tierName.equals("")){
			tier = "All";
			logger.info("Not passed any tier parameter / not populated properly");}
		else
			tier = tierName.toString();

		logger.info("Before the getATGEnvsStatsJSON call");
		logger.info("Start Time: "+ LocalDateTime.now());
		if (stopWatch.isStopped())
			stopWatch.start();
		List<JSONObject> tempList=  healthCheckATG.getAllATGEnvUrls(tier);
		if(stopWatch.isStarted()) {
			stopWatch.stop();
			logger.info("Total time taken: " + stopWatch.toString());
			stopWatch.reset();
		}
		return ResponseEntity.ok(tempList);
	}

	@CrossOrigin(allowedHeaders = "Access-Control-Allow-Origin")
	@RequestMapping(value = {"/dummyMode","/dummyMode/{tierName}"}, method = RequestMethod.GET)
	public ResponseEntity<List<JSONObject>> getATGEmailDummyModeCheck(@PathVariable(value = "tierName",required = false) String tierName) {
		String tier = "All";
		if(tierName==null || tierName.equals(null) || tierName.equals("")){
			tier = "All";
			logger.info("Not passed any tier parameter / not populated properly");}
		else
			tier = tierName.toString();

		logger.info("Before the generateEmailDummyModeValues call");
		logger.info("Start Time: "+ LocalDateTime.now());
		if (stopWatch.isStopped())
			stopWatch.start();
		List<JSONObject> tempList=  emailDummyModeCheckService.generateEmailDummyModeValues(tier);
		if(stopWatch.isStarted()) {
			stopWatch.stop();
			logger.info("Total time taken: " + stopWatch.toString());
			stopWatch.reset();
		}
		return ResponseEntity.ok(tempList);
	}

	@CrossOrigin(allowedHeaders = "Access-Control-Allow-Origin")
	@RequestMapping(value = {"/atgSMSFTValues","/atgSMSFTValues/{tierName}"}, method = RequestMethod.GET)
	public ResponseEntity<List<JSONObject>> getATGSMSFTValues(@PathVariable(value = "tierName",required = false) String tierName) {
		String tier = "All";
		if(tierName==null || tierName.equals(null) || tierName.equals("")){
			tier = "All";
			logger.info("Not passed any tier parameter / not populated properly");}
		else
			tier = tierName.toString();
		logger.info("Before the generateSMSFeatureToggleValues call For tier: "+tier);
		logger.info("Start Time: "+ LocalDateTime.now());
		if (stopWatch.isStopped())
			stopWatch.start();
		List<JSONObject> tempList=  smsFeatureToggleCheck.generateSMSFeatureToggleValues(tier);
		if(stopWatch.isStarted()) {
			stopWatch.stop();
			logger.info("Total time taken: " + stopWatch.toString());
			stopWatch.reset();
		}
		return ResponseEntity.ok(tempList);
	}

	@CrossOrigin(allowedHeaders = "Access-Control-Allow-Origin")
	@RequestMapping(value = {"/atgMockValues","/atgMockValues/{tierName}"}, method = RequestMethod.GET)
	public ResponseEntity<List<JSONObject>> getMockValues(@PathVariable(value = "tierName",required = false) String tierName) {
		String tier = "All";
		if(tierName==null || tierName.equals(null) || tierName.equals("")){
			tier = "All";
			logger.info("Not passed any tier parameter / not populated properly");}
		else
			tier = tierName.toString();
		logger.info("Before the getMockValues call");
		if (stopWatch.isStopped())
			stopWatch.start();
		logger.info("Start Time: "+ LocalDateTime.now());
		List<JSONObject> tempList= mockCheckService.getMockValues(tier);
		if(stopWatch.isStarted()) {
			stopWatch.stop();
			logger.info("Total time taken: " + stopWatch.toString());
			stopWatch.reset();
		}
		logger.info("End Time: "+ LocalDateTime.now());
		return ResponseEntity.ok(tempList);
	}

	@CrossOrigin(allowedHeaders = "Access-Control-Allow-Origin")
	@RequestMapping(value = {"/allData"}, method = RequestMethod.GET)
	public ResponseEntity<JSONObject> getAllData() {
		String tier = "All";

		logger.info("Before the getting all stats call");
		if (stopWatch.isStopped())
			stopWatch.start();
		logger.info("Start Time: "+ LocalDateTime.now());
		JSONObject tempList= parallelCallOfSmsMockEmail.parallelCall();
		if(stopWatch.isStarted()) {
			stopWatch.stop();
			logger.info("Total time taken: " + stopWatch.toString());
			stopWatch.reset();
		}
		return ResponseEntity.ok(tempList);
	}
//, origins = "http://127.0.0.1:8887" ,originPatterns = "*/*"
	//@CrossOrigin(allowedHeaders = "Access-Control-Allow-Origin", origins = "http://127.0.0.1:8887", originPatterns = "*")
	@CrossOrigin(allowedHeaders = "*", origins = "*")
	@RequestMapping(value = {"/enableDisableIncIdx"}, method = RequestMethod.POST)
	public ResponseEntity<List<JSONObject>> enableDisableIncIndex(@RequestBody JSONObject details) {

		logger.info("Before the toggleEnableDisableIncrIndexing call");
		logger.info("Start Time: "+ LocalDateTime.now());
		List<JSONObject> tempList = null;
		try {
			if (stopWatch.isStopped())
				stopWatch.start();
			tempList = enableDisableIncrIndexing.toggleEnableDisableIncrIndexing(
					details.get("environment").toString(), details.get("enabledFlag").toString()
			);
			if(stopWatch.isStarted()) {
				stopWatch.stop();
				logger.info("Total time taken: " + stopWatch.toString());
				stopWatch.reset();
			}
		}catch (Exception e){
			stopWatch.stop();
			logger.info("Exception Occured Total time taken: " + stopWatch.toString());
			stopWatch.reset();
			tempList = null;
			return ResponseEntity.unprocessableEntity().body(tempList);
		}
		return ResponseEntity.ok(tempList);
	}
}
