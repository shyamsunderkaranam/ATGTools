package com.atg.atgtools.conf;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

@Component
public class EnvDataDAO {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void putEnvStatsData(JSONArray data, Path filePath) {

		try 
        {
			if(Files.exists(filePath)) {
				  FileChannel.open(filePath, StandardOpenOption.WRITE).truncate(0).close();
			  }
			  Files.write(filePath, data.toString().getBytes());
			  
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
		
	}
	
	public void putEnvStatsData(JSONObject data, Path filePath) {

		try 
        {
			if(Files.exists(filePath)) {
				  FileChannel.open(filePath, StandardOpenOption.WRITE).truncate(0).close();
			  }
			  Files.write(filePath, data.toString().getBytes());
			  
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
		
	}
	
	public JSONArray getData(String configFilePath) {

		JSONParser jsonParser = new JSONParser();
		JSONArray ja=null;
		Object obj = null;
		try (FileReader reader = new FileReader(configFilePath))
        {
            //Read JSON file
			obj =  jsonParser.parse(reader);;
			ja= (JSONArray)obj;
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
        	ja = new JSONArray();
        	ja.add( obj);
        } catch (Exception e) {
        	 e.printStackTrace();
        }
		return ja;
	}
	public JSONObject getJSONData(String configFilePath) {

		JSONParser jsonParser = new JSONParser();
		JSONObject ja=null;
		try (FileReader reader = new FileReader(configFilePath))
        {
            //Read JSON file
			ja= (JSONObject) jsonParser.parse(reader);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
		return ja;
	}
	


}
