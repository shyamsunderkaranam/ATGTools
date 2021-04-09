package com.atg.atgtools.sched;

import com.atg.atgtools.conf.EnvDataDAO;
import com.atg.atgtools.mail.EmailSenderResource;
import com.atg.atgtools.services.CheckAvailability;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ATGEnvCheckScheduler {

	private static final Logger log = LoggerFactory.getLogger(ATGEnvCheckScheduler.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final String configFilePath="config/monitoringConf.json";
	private static final String ATG_ENV_STATS_ERR_FILE = "EnvData/Atg_Env_Err_State.json";
	private EnvDataDAO envData;
	JSONObject jo;
	//private long envCheckInterval, envCommInterval;

	
	public ATGEnvCheckScheduler() {

		/*envCheckInterval = (long) jo.get("envCheckInterval");
		envCommInterval = (long) jo.get("envCommInterval");*/

	}


	//@Scheduled(fixedRate = 300000)
	  public void reportATGEnvStats() {

	    log.info("The time is {}", dateFormat.format(new Date()));
	    
	    CheckAvailability ca= new CheckAvailability();
		  
		  try { 
			  ca.getUrlAvailabilityStats("all");
			  
		  }
		  catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace();
			  System.out.println("Something happened");
			  e.printStackTrace();
		  }
	  }
	
	
	  @Scheduled(fixedRate = 150000) public void sendCommsATGEnvErrStats() {
		envData = new EnvDataDAO();
		JSONArray ja=envData.getData(configFilePath);
		jo = (JSONObject)ja.get(0);
		
		try {
		  
		if(jo.get("sendEmail").toString().equals("Y")) {
		  log.info("Sending error report. The time is now {}", dateFormat.format(new
		  Date()));
		  
		  EmailSenderResource sendEmail = new EmailSenderResource(); String
		  messageBody=jo.get("commBody").toString();
		  messageBody.concat(envData.getData(ATG_ENV_STATS_ERR_FILE).toJSONString());
		  String
		  commSubject=jo.get("commSubject").toString().concat(dateFormat.format(new
		  Date()));
		  messageBody.concat(envData.getData(ATG_ENV_STATS_ERR_FILE).toJSONString());
		  
		  try {
		  sendEmail.sendMimeMessageWOSpring(jo.get("commDL").toString(),commSubject ,
		  messageBody);
		  
		  } catch (Exception e) { // TODO Auto-generated catch block
		  e.printStackTrace(); System.out.println("Something happened");
		  e.printStackTrace(); }
		  
		}else {
			System.out.println("Email sending is disabled");
		}
	  
	  
	  }catch(Exception e) {
		  e.printStackTrace();
	  }
	  }
	 
}
