package com.atg.atgtools.services;

import com.atg.atgtools.conf.EnvDataDAO;
import com.sun.mail.imap.Rights;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import scala.util.parsing.json.JSON;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PrepareATGLinksService {

    Logger logger = LoggerFactory.getLogger(PrepareATGLinksService.class);
    private JSONObject envJSONObject, personaJSONObject;
    private static final String ATG_ENV_STATS_FILE = "EnvData/Atg_Env_State.json";
    private static final String ATG_ENV_STATS_ERR_FILE = "EnvData/Atg_Env_Err_State.json";
    private static final String ATG_ENV_CONFIG_FILE="config/tier_envs.json";
    private EnvDataDAO envData;
    private String tierName;
    List<JSONObject> agtSilos,backOfc,stfSilos,genLinks;

    public String getDesiredLink(JSONObject env, JSONObject persona, String siloNumber) {

        String protocol = "http://atg-";
        //String protocol = "atg-";
        String desiredLink = "";
        //System.out.println(env +" "+persona);
        if(persona.get("isDynAdmin").toString().equalsIgnoreCase("Y")) {

            String postExtension = "/dyn/admin";
            String personaID= persona.get("persId").toString();
            String portNo = persona.get("port").toString();
            
            if(persona.get("persona").equals("Agent") ) {

                personaID = env.get("agentPersona").toString();
                personaID = personaID.concat(siloNumber);
            }
            if(persona.get("persona").equals("storefront") ) {

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
            if(persona.get("persona").equals("atgbcc")){
                String postExtension = "/atg/bcc";
                String personaID = "pub01";
                String portNo = "8070";
                desiredLink = protocol + env.get("envId")+"-"+env.get("hosted")+"-"+personaID+env.get("postString")+":"+portNo+postExtension;
            }
            //System.out.println(desiredLink);

        }

        return desiredLink;
    }

    public List<JSONObject> getAllATGEnvUrls(String tierNames) {

        logger.info(Thread.currentThread().getName()+" Preparing the ATG environment links now");
        envData = new EnvDataDAO();
        JSONArray config = envData.getData(ATG_ENV_CONFIG_FILE);
        envJSONObject = (JSONObject) config.get(0);
        personaJSONObject = (JSONObject) config.get(1);
        JSONArray tiers = (JSONArray) envJSONObject.get("tiers");

        agtSilos = new ArrayList<>();
        backOfc = new ArrayList<>();
        stfSilos = new ArrayList<>();
        genLinks = new ArrayList<>();
        Stream stream = tiers.stream()
                .map(obj ->  {
                    JSONObject tempJsonobj = (JSONObject) obj ;
                    if(tempJsonobj.get("tier").toString().equalsIgnoreCase(tierNames) ||
                            tierNames.equalsIgnoreCase("All"))
                        return tempJsonobj;
                    return null;
                })

                ;

        List<JSONObject> tempJSONObjectList = new ArrayList<JSONObject>();
        List<List> tempListObject=  ((Stream<JSONObject>) stream)
                //.map(this::createEnvironmentDetails)
                //.parallel()
                .filter(envTier -> envTier!= null)
                .map(envTier -> createEnvironmentDetailsfromTier(envTier))
                .collect(Collectors.toList());

        tempListObject.stream()
                .forEach(tempList -> {
                    tempJSONObjectList.addAll(tempList);
                });
        /*
        if (!tierNames.equalsIgnoreCase("All")){
            List<JSONObject> tempJSONObjectListFiltered = tempJSONObjectList.stream()
                    .filter(tempJSONObject -> tempJSONObject.get("tier").toString().equalsIgnoreCase(tierNames))
                    .collect(Collectors.toList());
            return tempJSONObjectListFiltered;
        } */
        return tempJSONObjectList;
    }



    public List<JSONObject> createEnvironmentDetailsfromTier(Object envTier){

        JSONObject tier;
        JSONArray atgenvs;

        logger.info("In createEnvironmentDetailsfromTier ");
        agtSilos = new ArrayList<>();
        backOfc = new ArrayList<>();
        stfSilos = new ArrayList<>();
        genLinks = new ArrayList<>();
        tier = (JSONObject)envTier;


        tierName = (String) tier.get("tier") ;
        //if (tierNames.equalsIgnoreCase("All")|| tierName.equalsIgnoreCase(tierNames)) {
        atgenvs = (JSONArray) tier.get("envs");

        Stream stream = atgenvs.stream().map(obj -> (JSONObject) obj);



        List<List> tempListObject=  ((Stream<JSONObject>) stream)
        .filter(envt -> ("N".equalsIgnoreCase(envt.get("excludeHealthCheck").toString())))
                .map(atgenvt -> createPersonaDetailsFromEnv(atgenvt))
                .parallel()
                .collect(Collectors.toList())
               ;
        List<JSONObject> tempJSONObject = new ArrayList<JSONObject>();
        tempListObject.stream()
                .forEach(tempList -> {
                    tempJSONObject.addAll(tempList);
                });
        return tempJSONObject;

    }

    public List createPersonaDetailsFromEnv(Object atgenvt){
        JSONArray personas = (JSONArray) personaJSONObject.get("personas");
        logger.info("In createPersonaDetailsFromEnv ");
        List<JSONObject> envLinks = new ArrayList<>();
        personas.forEach(persona1 -> {

            JSONObject persona = (JSONObject) persona1;
        String siloNumber = "";
        JSONObject atgenv = (JSONObject) atgenvt;
        

        int linkAvailability = 0;


        JSONObject envStateJSON=null;
        String desiredLink;

        if (persona.get("persId").equals("app")) {
            for (int l = 0; l < (long) atgenv.get("agentsilos"); l++) {

                envStateJSON = new JSONObject();
                envStateJSON.put("tier", tierName);
                envStateJSON.put("environment", atgenv.get("environments").toString());
                envStateJSON.put("State", linkAvailability);

                linkAvailability = 0;
                try {
                    siloNumber = String.format("%02d", l + 1);
                    desiredLink = getDesiredLink(atgenv, persona, siloNumber);
                    envStateJSON.put("persona", persona.get("persona").
                            toString().concat(siloNumber));

                    envStateJSON.put("Link", desiredLink);

                    if (persona.get("persona").toString().equalsIgnoreCase("storefront"))
                        stfSilos.add(envStateJSON);
                    if (persona.get("persona").toString().equalsIgnoreCase("Agent"))
                        agtSilos.add(envStateJSON);

                    envLinks.add(envStateJSON);
                } catch (Exception e) {
                    e.printStackTrace();

                }


            }
        } else if(!(atgenv.get("tradepoint").equals("NA") && persona.get("persId").toString().equalsIgnoreCase("Tradepoint"))) {

            //envStateJSON = new JSONObject();
            //envStateErrJSON = new JSONObject();
            envStateJSON = new JSONObject();
            envStateJSON.put("tier", tierName);
            envStateJSON.put("environment", atgenv.get("environments").toString());
            envStateJSON.put("State", linkAvailability);

            desiredLink = getDesiredLink(atgenv, persona, "");
            //logger.info(Thread.currentThread().getName() + " In getUrlAvailabilityStats function - TP Conditon: "+desiredLink);
            envStateJSON.put("Link", desiredLink);
            envStateJSON.put("persona",persona.get("persona").toString());

            if (persona.get("isDynAdmin").toString().equalsIgnoreCase("Y"))
                backOfc.add(envStateJSON);
            if (persona.get("isDynAdmin").toString().equalsIgnoreCase("N"))
                genLinks.add(envStateJSON);
            envLinks.add(envStateJSON);


        } // if(persona.get("persId").equals("app") ) else ends here
                        //return (JSONObject)envStateJSON   ;
        })   ;
        return envLinks;
    }
/*
    public static void main(String[] args) {
        PrepareATGLinksService prepareATGLinksService =  new PrepareATGLinksService();
        //System.out.println(prepareATGLinksService.getAllATGEnvUrls("Q"));
        System.out.println(prepareATGLinksService.getAllATGEnvUrls("All"));
    }

 */

}
