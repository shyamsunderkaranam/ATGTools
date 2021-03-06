package com.atg.atgtools.controllers;

import com.atg.atgtools.AtgtoolsApplication;
import com.atg.atgtools.services.CheckAvailability;
import com.atg.atgtools.services.HealthCheckATG;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class ATGToolsController {

	Logger logger = LoggerFactory.getLogger(ATGToolsController.class);
	@Autowired
	HealthCheckATG healthCheckATG;

	@RequestMapping("/jaiganesh")
	public ResponseEntity<String> testMethod(){

		return ResponseEntity.ok("JAI GANESH");
	}

	@RequestMapping("/atgEnvStats")
	public List<JSONObject> getATGEnvAvailability() {
		logger.info("Before the getATGEnvsStatsJSON call");
		logger.info("Start Time: "+ LocalDateTime.now());
		return healthCheckATG.getAllATGEnvUrls("all");
	}
}
