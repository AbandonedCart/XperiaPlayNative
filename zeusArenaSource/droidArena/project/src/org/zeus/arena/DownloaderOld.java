package org.zeus.arena;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
 
import org.apache.http.util.ByteArrayBuffer;
import org.zeus.arena.R;

public class DownloaderOld extends Activity{
	
	//private String openArenaDownloadPath = "http://www.moddb.com/downloads/mirror/12708/40/435a2e7a70a024f21b29d85cbcdf16d1";	//link 1
	//private String openArenaDownloadPath = "http://www.gamershell.com/download.html?id=35268&mirror=17&cat=demo&filename=oa081.zip&ts=1315900042&auth=6jijprbj"	//link 2
	//private String openArenaDownloadPath = "http://www.turbosquid.com/Download/Index.cfm?FuseAction=Download&ID=204353_436569";
	private String openArenaDownloadPath = "http://media1.gamefront.com/moddb/2008/10/31/oa081.zip?b17f4b620c6cf1393ffa644d1ceea1519471f50243241c9c351f544aefaeb617054856f45e07ae230795c14b30a53906a278cc670925e173f731b5fc3bbd23898e42daf546aadd3a9b7203ffda3ef4ff7bcea830a52cb9225e35d112e02338ab10ad687879c890815afc78590360911325ec";	//link 3
	private String openArenaTempStoragePath;
	private String openArenapk3Path;
	private String openArenaPath;
	private long percentageIncrements = 3189243;
	private List<String> pk3Paths;
	private ProgressBar progressBar;
	private TextView textView;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		textView = (TextView)findViewById(R.id.textView1);
		
		openArenaPath = Environment.getExternalStorageDirectory().getPath() + "/quake3/";
		if (!folderExists(openArenaPath)){
			makeFolder(openArenaPath);
		}
		openArenaTempStoragePath = openArenaPath + "temp/";
		if (!folderExists(openArenaTempStoragePath)){
			makeFolder(openArenaTempStoragePath);
		}
		openArenapk3Path = openArenaPath + "baseq3/";
		if (!folderExists(openArenapk3Path)){
			makeFolder(openArenapk3Path);
		}
		pk3Paths = new Vector<String>();
		pk3Paths.add(openArenaTempStoragePath + "openarena-0.8.1/missionpack/" + "mp-pak0.pk3");
		pk3Paths.add(openArenaTempStoragePath + "openarena-0.8.1/baseoa/" + "pak0.pk3");
		pk3Paths.add(openArenaTempStoragePath + "openarena-0.8.1/baseoa/" + "pak1-maps.pk3");
		pk3Paths.add(openArenaTempStoragePath + "openarena-0.8.1/baseoa/" + "pak2-players.pk3");
		pk3Paths.add(openArenaTempStoragePath + "openarena-0.8.1/baseoa/" + "pak2-players-mature.pk3");
		pk3Paths.add(openArenaTempStoragePath + "openarena-0.8.1/baseoa/" + "pak4-textures.pk3");
		pk3Paths.add(openArenaTempStoragePath + "openarena-0.8.1/baseoa/" + "pak5-TA.pk3");
		pk3Paths.add(openArenaTempStoragePath + "openarena-0.8.1/baseoa/" + "pak6-misc.pk3");
	}
	
	public boolean getOpenArena(){
		boolean result = false;
		File zipFile = download();
		unzip(zipFile);
		moveFiles();
		File tempDirectory = new File(openArenaTempStoragePath);
		tempDirectory.delete();
		return result;
	}
	
	public File download(){
		File openArenaZip = null;
		try{			
			URL openArenaURL = new URL(openArenaDownloadPath);
			URLConnection connection = openArenaURL.openConnection();
			InputStream inputStream = connection.getInputStream();
			BufferedInputStream downloadStream = new BufferedInputStream(inputStream);
			openArenaZip = new File(openArenaTempStoragePath + "test.zip");
			FileOutputStream fileWriter = new FileOutputStream(openArenaZip);
			int data = 0;
			int count = 0;
			int percentageDone = 0;
			while ((data = downloadStream.read()) != -1){	//download data
				fileWriter.write((byte) data);
				if (count == percentageIncrements){
					count = -1;
					percentageDone ++;
					progressBar.setProgress(percentageDone);
					Log.d("DROID_ARENA", percentageDone + "% done.");
				}
				count++;
			}
			fileWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return openArenaZip;
	}
	
	public boolean unzip(File file){
		boolean result = false;
		try{
			textView.setText("Extracting...");
			progressBar.setProgress(0);
			FileInputStream fileInputStream = new FileInputStream(file); 
			ZipInputStream zipStream = new ZipInputStream(fileInputStream);
			ZipEntry zipContents = null; 
			
			while ((zipContents = zipStream.getNextEntry()) != null){ 		 
		        if(zipContents.isDirectory()){
		        	if (!folderExists(openArenaTempStoragePath + zipContents.getName())){
		    			makeFolder(openArenaTempStoragePath + zipContents.getName());
		    		} 
		        }
		        else{
		        	int data = 0;
		        	FileOutputStream fileOutputStream = new FileOutputStream(openArenaTempStoragePath + zipContents.getName());
		        	while ((data = zipStream.read()) != 1){
		        		fileOutputStream.write(data);
		        	}
		        	zipStream.closeEntry();
		        	fileOutputStream.close();
		        }		         
		      }
			zipStream.close();
			result = true;
		}
		catch (Exception e){
			e.printStackTrace();
		} 
		return result;
	}
	
	private boolean moveFiles(){
		boolean result = false;
		for (String path : pk3Paths){
			moveFile(path, openArenapk3Path);
		}
		return result;
	}
	
	private boolean moveFile(String oldPath, String newDirectory){
		File file = new File(oldPath);
		File directory = new File(newDirectory);
		return file.renameTo(new File(directory, file.getName()));
	}
	
	private boolean folderExists(String path) {
    	File folder = new File(path);
    	return folder.isDirectory();
      } 
    
    private boolean makeFolder(String path){
    	File folder = new File(path);
    	return folder.mkdirs();
    }
}
