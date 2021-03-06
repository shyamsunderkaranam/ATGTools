package com.atg.atgtools.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.atg.atgtools.conf.EnvDataDAO;

public class SmsFeatureToggleCheck {

	private  String dynAdminString = "/nucleus/kf/common/featuretoggles/DefaultFeatureToggles/?propertyName=featureToggles.enableSMSService" ;
	private JSONObject envJSONObject, personaJSONObject;
	private static final String ATG_ENV_STATS_FILE = "EnvData/Atg_Env_State.json";
	private static final String ATG_ENV_STATS_ERR_FILE = "EnvData/Atg_Env_Err_State.json";
	private static final String ATG_ENV_CONFIG_FILE="config/tier_envs.json";
	private EnvDataDAO envData;
	private String tierName;
	
	public SmsFeatureToggleCheck() {
		
	}
	public String getSMSFeatureToggleValue(String url) {
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			
			//Element content = doc.getElementById("content");
			Elements content = doc.getElementsByAttributeValueMatching("style", "white-space:pre") ;
			System.out.println("For url "+url+" Value: "+content.html());
			return  content.html();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
		
	}
	public static void main(String[] args) {
		LocalDateTime startTime = LocalDateTime.now();
		SmsFeatureToggleCheck smsCheck= new SmsFeatureToggleCheck();
		System.out.println(smsCheck.generateSMSFeatureToggleValues("All"));
		LocalDateTime endTime = LocalDateTime.now();
		System.out.println("Start time: "+startTime);
		System.out.println("End time: "+endTime);

	}
	
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
			
			/*
			 * if(env.get("aws").toString().equals("Y")) { hosted = "aws";
			 * 
			 * }
			 */
			
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
	
	public JSONArray generateSMSFeatureToggleValues(String tierNames) {
		envData = new EnvDataDAO();
		JSONArray config = envData.getData(ATG_ENV_CONFIG_FILE);
		envJSONObject = (JSONObject) config.get(0);
		personaJSONObject = (JSONObject) config.get(1);
		JSONArray tiers = (JSONArray) envJSONObject.get("tiers");
		Iterator<JSONArray> tiersIterator= tiers.iterator() ;
		JSONArray envStateArray = new JSONArray();
		JSONObject envStateJSON;
		//System.out.println(tiers);
		JSONObject tier, atgenv;
		JSONArray atgenvs,personas;
		String desiredLink="";
		personas = (JSONArray) personaJSONObject.get("personas");
		Iterator<JSONArray> personasIterator;
		int i=0;
		//for(int i=0;tiersIterator.hasNext();i++) 
		do{
			
			
			tier = (JSONObject) tiers.get(i);
			//System.out.println(tier);
			String tierName = (String) tier.get("tier") ;
			if (tierNames.equalsIgnoreCase("All")|| tierName.equalsIgnoreCase(tierNames)) {
				atgenvs = (JSONArray) tier.get("envs");
				Iterator<JSONArray> envIterator = atgenvs.iterator();
				int j=0;
				//for(int j=0;envIterator.hasNext();j++) {
				do {
					atgenv = (JSONObject) atgenvs.get(j);
					
					//System.out.println(atgenv);
					personasIterator= personas.iterator() ;
					for(int k=0;personasIterator.hasNext();k++) {
						JSONObject persona = (JSONObject)personas.get(k);
						String siloNumber = "";
						
						//int linkAvailability = 0 ;
						String smsFTValue = "default";
						if(!persona.get("persona").equals("AgentLB") && !persona.get("persona").equals("storefrontLB") && !persona.get("persona").equals("Tradepoint") && !persona.get("persona").equals("atgbcc") && atgenv.get("excludeSMSCheck").equals("N")){
						if(persona.get("persId").equals("app") ) {
							for (int l=0;l < (long)atgenv.get("agentsilos");l++) {
								
								envStateJSON = new JSONObject();
								//linkAvailability = 0 ;
								try {
									siloNumber = String.format("%02d", l+1);
									desiredLink = getDesiredLink(atgenv, persona, siloNumber).concat(dynAdminString);
									smsFTValue =  getSMSFeatureToggleValue(desiredLink);
									
									System.out.println("Tier: "+tierName+" Dyn Amin Link: "+desiredLink + " Availability is: "+smsFTValue);
								} catch (Exception e) {
									// TODO Auto-generated catch block
//									linkAvailability = 500;
									System.out.println("Not reachable. Tier: "+tierName+" Availability is: "+smsFTValue+" Dyn Amin Link: "+desiredLink );
									
								}
								envStateJSON.put("tier", tierName);
								envStateJSON.put("environment", atgenv.get("environments"));
								envStateJSON.put("opco", atgenv.get("opco"));
								envStateJSON.put("persona", persona.get("persona").toString().concat(siloNumber));
								envStateJSON.put("SMSFTValue", smsFTValue);
								envStateJSON.put("Link", desiredLink);
								envStateArray.add(envStateJSON);

							}
						}else {
							
							envStateJSON = new JSONObject();
							desiredLink = getDesiredLink(atgenv, persona, "").concat(dynAdminString);
							//System.out.println("Check point: "+desiredLink+ " "+persona.get("persId"));
							if(desiredLink != "" ) {
							
								try {
									smsFTValue =  getSMSFeatureToggleValue(desiredLink);
									System.out.println("Tier: "+tierName+" Dyn Amin Link: "+desiredLink + " Availability is: "+smsFTValue);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									//linkAvailability = 500;
									System.out.println("Not reachable. Tier: "+tierName+" Availability is: "+smsFTValue+" Dyn Amin Link: "+desiredLink );
									//e.printStackTrace();
								}
								
							}
							envStateJSON.put("tier", tierName);
							envStateJSON.put("environment", atgenv.get("environments"));
							envStateJSON.put("opco", atgenv.get("opco"));
							envStateJSON.put("persona", persona.get("persona").toString());
							envStateJSON.put("SMSFTValue", smsFTValue);
							envStateJSON.put("Link", desiredLink);
							envStateArray.add(envStateJSON);
						}
						
						}
						
						personasIterator.next();
					}
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
		return envStateArray;
	}

}
