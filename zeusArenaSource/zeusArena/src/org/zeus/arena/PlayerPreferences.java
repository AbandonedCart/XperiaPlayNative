package org.zeus.arena;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.zeus.arena.R;

import android.util.Log;

public class PlayerPreferences {
	
	private static PlayerPreferences thePlayerPreferences;
	private File progressFile;
	private int screenWidth;
	private int screenHeight;
	private Map<String, List<String>> preferences;
	private Map<String, List<String>> defaultPreferences;
	private final int defaultAlpha = 175;
	
	private PlayerPreferences(int screenWidth, int screenHeight){
		this.screenHeight = screenHeight;
		this.screenWidth = screenWidth;	
		populateDefaults();
		preferences = new Hashtable<String, List<String>>();
		try {
			progressFile = new File("/data/data/org.zeus.arena/lib" + "progressFile");
			if (!progressFile.exists()){
				restoreDefaults();
			}
			InputStream inputStream = new FileInputStream(progressFile);
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
			String line;
			while ((line = bufferedReader.readLine()) != null){
				String preference = "";
				int i = 0;
				while (line.charAt(i) != ':'){
					preference += line.charAt(i);
					i++;
				}
				i++;
				List<String> values = new Vector<String>();
				while (line.charAt(i) != '.'){
					String value = "";
					while (line.charAt(i) != ','){
						value += line.charAt(i);
						i++;
					}
					values.add(value);
					i++;
				}
				preferences.put(preference, values);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		checkPreferences();
	}
	
	public void checkPreferences(){
		for (String preference : defaultPreferences.keySet()){
			if (!preferences.containsKey(preference)){
				updatePreference(preference, defaultPreferences.get(preference));
			}
		}
	}
	
	public List<String> getPreference(String preference){
		return preferences.get(preference);
	}
	
	public boolean preferenceOn(String preference){
		boolean result = true;
		List<String> preferences = PlayerPreferences.getThePlayerPreferences().getPreference(preference);
		if(preferences.get(0).equals("On")){	//if vibrations are on
			result = true;
		}
		else{
			result = false;
		}
		return result;
	}
	
	public boolean updatePreference(String preference, List<String> values){
		preferences.put(preference, values);
		boolean result = writePreferences();
		if (result){
			Persistence.getPersistence().getGame().refreash();
		}
		return result;
	}
	
	public boolean setPreferenceOn(String preference, boolean value){
		List<String> values = new Vector<String>();
		if (value){
			values.add("On");
		}
		else{
			values.add("Off");
		}
		preferences.put(preference, values);
		boolean result = writePreferences();
		if (result){
			Persistence.getPersistence().getGame().refreash();
		}
		return result;
	}
	
	public static void makeThePlayerPreferences(int screenWidth, int screenHeight){
		if (thePlayerPreferences == null){
			thePlayerPreferences = new PlayerPreferences(screenWidth, screenHeight);
		}
	}
	
	public static PlayerPreferences getThePlayerPreferences(){
		return thePlayerPreferences;
	}
	
	public boolean writePreferences(){
		boolean result = false;
		try {
			FileWriter fileWriter = new FileWriter(progressFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (String preference : preferences.keySet()){
				String line = preference + ":";
				for (String value : preferences.get(preference)){
					line += value + ",";
				}
				line = line.substring(0, line.length());
				line += ".\n";
				bufferedWriter.write(line);
			}
			bufferedWriter.close();
			result = true;
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	public void populateDefaults(){
		defaultPreferences = new Hashtable<String, List<String>>();
		defaultPreferences.put("Scheme", Arrays.asList("Improved"));
		defaultPreferences.put("Fire", Arrays.asList("On", "70", "70", ""+(screenWidth-70-60), ""+(screenHeight-70-60), ""+defaultAlpha));
		defaultPreferences.put("ImpFire", Arrays.asList("On", "115", "115", ""+(115+30), ""+(screenHeight-115-30), ""+defaultAlpha));
		defaultPreferences.put("StatFire", Arrays.asList("On", "30", "45", ""+(30), ""+(screenHeight-30), ""+defaultAlpha));
		defaultPreferences.put("Left", Arrays.asList("On", "100", "133", ""+(100+20), ""+(screenHeight-100-20), ""+defaultAlpha));
		defaultPreferences.put("Jump", Arrays.asList("On", "30", "45", ""+(screenWidth-30-15),""+(screenHeight-30-15), ""+defaultAlpha));
		defaultPreferences.put("ImpJump", Arrays.asList("On", "60", "90", ""+(screenWidth-60-25),""+(screenHeight/2), ""+defaultAlpha));
		defaultPreferences.put("Item", Arrays.asList("On", "50", "50", ""+((screenWidth/2) - 50), ""+50, ""+defaultAlpha));
		defaultPreferences.put("Weapon", Arrays.asList("On", "50", "50", ""+(screenWidth-50), ""+50, ""+defaultAlpha));
		defaultPreferences.put("Look", Arrays.asList("On"));
		defaultPreferences.put("Sound", Arrays.asList("On"));
		defaultPreferences.put("LightMaps", Arrays.asList("On"));
		defaultPreferences.put("FPS", Arrays.asList("Off"));
		defaultPreferences.put("PureServers", Arrays.asList("Off"));
		defaultPreferences.put("Vibrations", Arrays.asList("On"));
		defaultPreferences.put("Swipe", Arrays.asList("On"));
		defaultPreferences.put("Protocol", Arrays.asList("Off"));
		defaultPreferences.put("Turning", Arrays.asList("Off"));
	}
	
	public boolean restoreDefaults(){
		boolean result = false;
		try {
			progressFile.createNewFile();
			preferences.clear();
			preferences.putAll(defaultPreferences);
			writePreferences();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
