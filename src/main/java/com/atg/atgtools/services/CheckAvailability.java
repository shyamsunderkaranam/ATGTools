package com.atg.atgtools.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.atg.atgtools.controllers.ATGToolsController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


import com.atg.atgtools.conf.EnvDataDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CheckAvailability {

	Logger logger = LoggerFactory.getLogger(CheckAvailability.class);
	private JSONObject envJSONObject, personaJSONObject;
	private static final String ATG_ENV_STATS_FILE = "EnvData/Atg_Env_State.json";
	private static final String ATG_ENV_STATS_ERR_FILE = "EnvData/Atg_Env_Err_State.json";
	private static final String ATG_ENV_CONFIG_FILE="config/tier_envs.json";
	private static final int CONN_TIME_OUT = 3000;
	private EnvDataDAO envData;
	private String tierName;

	/*
	  public static void main(String[] args) { // TODO Auto-generated method stub
	  CheckAvailability ca= new CheckAvailability();
	  
	  try { 
		  //System.out.println(LocalDateTime.now());
			//int resp= ca.getResponseCodeForURLUsing("http://google.com","HEAD");
		    //System.out.println(resp);
		   //System.out.println(LocalDateTime.now());
		  //System.out.println("Returned JSON is: "+ca.getUrlAvailabilityStats("all"));
		  LocalDateTime startTime = LocalDateTime.now();
		  System.out.println(ca.getUrlAvailabilityStats("all"));
		  LocalDateTime endTime = LocalDateTime.now();
		  System.out.println("Start time: "+startTime);
		  System.out.println("End time: "+endTime);
		  }
	  catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace();
		  System.out.println("Something happened");
		  e.printStackTrace();
	  }
	  
	  }

	 */
	 
	
	public int getResponseCodeForURL(String address) throws IOException {
        return getResponseCodeForURLUsing(address, "GET");
    }

    @Async
    public int getResponseCodeForURLUsingHead(String address) throws IOException {
        return getResponseCodeForURLUsing(address, "HEAD");
    }
	@Async
	public int getResponseCodeForURLUsing(String address, String method) throws IOException {
        final URL url = new URL(address);
        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestMethod(method);
        
        huc.setConnectTimeout(CONN_TIME_OUT);
         huc.setReadTimeout(CONN_TIME_OUT);
         huc.setRequestMethod(method);
         huc.connect();
        return huc.getResponseCode();
	}

	@Async
	public String getDesiredLink(JSONObject env,JSONObject persona,String siloNumber) {
		
		String protocol = "http://atg-";
		String desiredLink = "";
		//System.out.println(env +" "+persona);
		if(!persona.get("persona").equals("AgentLB") && !persona.get("persona").equals("storefrontLB") && !persona.get("persona").equals("Tradepoint") ) {
			
			String postExtension = "/dyn/admin";
			String personaID= persona.get("persId").toString();
			String portNo = persona.get("port").toString();
			if(persona.get("persona").equals("atgbcc")){
				postExtension = "/atg/bcc";
				personaID = "pub01";
				portNo = "8070";
			}
			if(persona.get("persona").equals("Agent") ) {
				
				personaID = env.get("agentPersona").toString();
				//System.out.println(persona.get("persona")+" "+persona.get("persona").equals("agent"));
				personaID = personaID.concat(siloNumber);
			}
			if(persona.get("persona").equals("storefront") ) {
				
				//System.out.println(persona.get("persona")+" "+persona.get("persona").equals("agent"));
				personaID = personaID.concat(siloNumber);
			}
			desiredLink = protocol + env.get("envId")+"-"+env.get("hosted")+"-"+personaID+env.get("postString")+":"+portNo+postExtension;
		}
		else {
			if(persona.get("persona").equals("AgentLB")) {
				desiredLink = env.get("agentLB").toString();
			}
			if(persona.get("persona").equals("storefrontLB")) {
				desiredLink = env.get("DIYLB").toString();
			}

			if(persona.get("persona").equals("Tradepoint")) {
				desiredLink = env.get("tradepoint").toString();
			}
			//System.out.println(desiredLink);

		}
		
		return desiredLink;
	}
	
	public JSONArray getATGEnvsStats(String tier) {
		
		envData = new EnvDataDAO();
		
		JSONArray ja = envData.getData(ATG_ENV_STATS_FILE);
		JSONArray toBeSent=new JSONArray();
		Iterator<JSONArray> tiersIterator= ja.iterator() ;
		int i=0;
		do {
			JSONObject jo = (JSONObject) ja.get(i);
			if(jo.get("tier").toString().equalsIgnoreCase(tier)) {
				toBeSent.add(jo);
			}
			i++;
			tiersIterator.next();
		}while(tiersIterator.hasNext());
		
		return toBeSent;
	}


	public JSONObject getATGEnvsStatsJSON(String tier) {
		
		envData = new EnvDataDAO();
		JSONObject finalOutput= new JSONObject();
		JSONObject wholeEnvStats = envData.getJSONData(ATG_ENV_STATS_FILE);
		JSONArray ja = (JSONArray) wholeEnvStats.get("EnvStatus");
		JSONArray toBeSent=new JSONArray();
		Iterator<JSONArray> tiersIterator= ja.iterator() ;
		int i=0;
		do {
			JSONObject jo = (JSONObject) ja.get(i);
			if(jo.get("tier").toString().equalsIgnoreCase(tier)) {
				toBeSent.add(jo);
			}
			i++;
			tiersIterator.next();
		}while(tiersIterator.hasNext());
		finalOutput.put("EnvStatus",toBeSent);
		finalOutput.put("LAST_MODIFIED",wholeEnvStats.get("LAST_MODIFIED"));
		return finalOutput;
	}


	@Async
	public CompletableFuture<JSONObject> getUrlAvailabilityStats(String tierNames) {
		
		logger.info(Thread.currentThread().getName()+" In getUrlAvailabilityStats function");
		envData = new EnvDataDAO();
		JSONArray config = envData.getData(ATG_ENV_CONFIG_FILE);
		envJSONObject = (JSONObject) config.get(0);
		personaJSONObject = (JSONObject) config.get(1);
		JSONArray tiers = (JSONArray) envJSONObject.get("tiers");
		Iterator<JSONArray> tiersIterator= tiers.iterator() ;
		JSONArray envStateArray = new JSONArray();
		JSONArray envStateErrArray = new JSONArray();
		JSONObject envStateJSON, envStateErrJSON;
		//System.out.println(tiers);
		JSONObject tier, atgenv;
		JSONArray atgenvs,personas;
		String desiredLink="";
		personas = (JSONArray) personaJSONObject.get("personas");
		Iterator<JSONArray> personasIterator;
		int i=0;
		List<JSONObject> allStatus = new ArrayList<JSONObject>();
		//for(int i=0;tiersIterator.hasNext();i++) 
		do{

			logger.info(Thread.currentThread().getName()+" In getUrlAvailabilityStats function - First Do while loop" );
			tier = (JSONObject) tiers.get(i);
			//System.out.println(tier);
			tierName = (String) tier.get("tier") ;
			if (tierNames.equalsIgnoreCase("All")|| tierName.equalsIgnoreCase(tierNames)) {
				atgenvs = (JSONArray) tier.get("envs");
				Iterator<JSONArray> envIterator = atgenvs.iterator();
				int j=0;
				//for(int j=0;envIterator.hasNext();j++) {
				do {
					logger.info(Thread.currentThread().getName()+" In getUrlAvailabilityStats function - Second Do while loop");
					atgenv = (JSONObject) atgenvs.get(j);
					
					personasIterator=null;
					if(atgenv.get("excludeSMSCheck").equals("N")) {
					//System.out.println(atgenv);
					personasIterator= personas.iterator() ;
					for(int k=0;personasIterator.hasNext();k++) {
						logger.info(Thread.currentThread().getName()+" In getUrlAvailabilityStats function - For loop");
						JSONObject persona = (JSONObject)personas.get(k);
						String siloNumber = "";
						
						int linkAvailability = 0 ;
						
						if(persona.get("persId").equals("app") ) {
							for (int l=0;l < (long)atgenv.get("agentsilos");l++) {
								
								envStateJSON = new JSONObject();
								envStateErrJSON = new JSONObject();
								linkAvailability = 0 ;
								try {
									siloNumber = String.format("%02d", l+1);
									desiredLink = getDesiredLink(atgenv, persona, siloNumber);
									//linkAvailability =  getResponseCodeForURLUsingHead(desiredLink);
									allStatus.add(getURLAvailability(desiredLink,
											atgenv.get("environments").toString(),
											tierName,persona.get("persona").
													toString().concat(siloNumber)));
									//System.out.println("Tier: "+tierName+" Dyn Amin Link: "+desiredLink + " Availability is: "+linkAvailability);
								} catch (Exception e) {
										e.printStackTrace();
									
								}


							}
						}else {
							
							envStateJSON = new JSONObject();
							envStateErrJSON = new JSONObject();
							desiredLink = getDesiredLink(atgenv, persona, "");
							//System.out.println("Check point: "+desiredLink+ " "+persona.get("persId"));
							if(desiredLink != "" ) {
							
								try {
									//linkAvailability =  getResponseCodeForURLUsingHead(desiredLink);
									//System.out.println("Tier: "+tierName+" Dyn Amin Link: "+desiredLink + " Availability is: "+linkAvailability);
									allStatus.add(getURLAvailability(desiredLink,
											atgenv.get("environments").toString(),
											tierName,persona.get("persona").
													toString().concat(siloNumber)));
								} catch (Exception e) {
									e.printStackTrace();
								}
								
							}

						} // if(persona.get("persId").equals("app") ) else ends here
						
						personasIterator.next();
					   } // for(int k=0;personasIterator.hasNext();k++)
					} //if(atgenv.get("excludeSMSCheck").equals("N"))
					envIterator.next();
					j++;
					//System.out.println(j);
				}while(envIterator.hasNext());
			}
			tiersIterator.next();
			i++;
			//System.out.println("JSON: "+envStateArray);
			//System.out.println("i is: "+i);
			//System.out.println(tiers.get(i));
		}while(tiersIterator.hasNext());
		//System.out.println("JSON: "+envStateArray);
		//return envStateArray;
		JSONObject finalOne = new JSONObject();
		JSONObject finalErrOne = new JSONObject();
		finalOne.put("LAST_MODIFIED", ""+LocalDateTime.now());
		//CompletableFuture.allOf(allStatus.toArray(new CompletableFuture[0])).join();
		//CompletableFuture.allOf((CompletableFuture<ArrayList>) allStatus).join();
		finalOne.put("EnvStatus", allStatus);
		//finalErrOne.put("LAST_MODIFIED", ""+LocalDateTime.now());
		//finalErrOne.put("EnvErrStatus", envStateErrArray);
		envData.putEnvStatsData(finalOne, Paths.get(ATG_ENV_STATS_FILE));
		//envData.putEnvStatsData(envStateArray, Paths.get(ATG_ENV_STATS_FILE));
		//envData.putEnvStatsData(envStateErrArray, Paths.get(ATG_ENV_STATS_ERR_FILE));
		//envData.putEnvStatsData(finalErrOne, Paths.get(ATG_ENV_STATS_ERR_FILE));
		return CompletableFuture.completedFuture(finalOne);
	}

	@Async
	public JSONObject getURLAvailability (String desiredLink, String atgEnv, String tier, String persona){

		JSONObject envStateJSON= new JSONObject();
		if(desiredLink != "" ) {
			int linkAvailability = 0;

			try {
					linkAvailability =  getResponseCodeForURLUsingHead(desiredLink);
					envStateJSON.put("tier", tier);
					envStateJSON.put("environment", atgEnv);
					envStateJSON.put("persona", persona);
					envStateJSON.put("State", linkAvailability);
					envStateJSON.put("Link", desiredLink);
					//envStateArray.add(envStateJSON);
					logger.info(Thread.currentThread().getName()+" Tier: "+tierName+" Dyn Amin Link: "+desiredLink + " Availability is: "+linkAvailability);
				} catch (Exception e) {
					//linkAvailability = 500;
					logger.info(Thread.currentThread().getName()+" Not reachable. Tier: "+tierName+" Availability is: "+linkAvailability+" Dyn Amin Link: "+desiredLink );
					envStateJSON.put("tier", tier);
					envStateJSON.put("environment", atgEnv);
					envStateJSON.put("persona", persona);
					envStateJSON.put("State", linkAvailability);
					envStateJSON.put("Link", desiredLink);
					//envStateErrArray.add(envStateErrJSON);
				}

		}
		return envStateJSON;

	}

}
