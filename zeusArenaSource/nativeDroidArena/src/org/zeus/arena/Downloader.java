package org.zeus.arena;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
 
import org.apache.http.util.ByteArrayBuffer;
import org.zeus.arena.R;

public class Downloader extends Activity{
	
	//private String openArenaDownloadPath = "http://www.moddb.com/downloads/mirror/12708/40/435a2e7a70a024f21b29d85cbcdf16d1";	//link 1
	//private String openArenaDownloadPath = "http://www.gamershell.com/download.html?id=35268&mirror=17&cat=demo&filename=oa081.zip&ts=1315900042&auth=6jijprbj"	//link 2
	//private String openArenaDownloadPath = "http://www.turbosquid.com/Download/Index.cfm?FuseAction=Download&ID=204353_436569";
	private String openArenaDownloadPath = "http://media1.gamefront.com/moddb/2008/10/31/oa081.zip?b17f4b620c6cf1393ffa644d1ceea1519471f50243241c9c351f544aefaeb617054856f45e07ae230795c14b30a53906a278cc670925e173f731b5fc3bbd23898e42daf546aadd3a9b7203ffda3ef4ff7bcea830a52cb9225e35d112e02338ab10ad687879c890815afc78590360911325ec";	//link 3
	private String openArenaTempStoragePath;
	private String openArenapk3Path;
	private String openArenaPath;
	private long percentageIncrements = 3189243;
	private List<String> pk3Paths;
	private int bufferCapacity = 5000000;	
	private int percentageDone;
	
	private ProgressBar progressBar;
	private TextView textView;
	private Handler mHandler = new Handler();
	private File progressFile;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);	//start
		
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

		new Thread(new Runnable() {
            public void run() {
            	getOpenArena();
            }
        }).start();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	        finish();
	        android.os.Process.killProcess(android.os.Process.myPid());
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public char getProgressFile(){
		char progress = 'F';
		try {
			progressFile = new File(openArenaPath + "/temp/" + "progressFile");
			if (!progressFile.exists()){
				progressFile.createNewFile();
				writeProgress('N');
			}
			InputStream inputStream = new FileInputStream(progressFile);
			progress = (char) inputStream.read();
			inputStream.read();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return progress;
	}
	
	public char writeProgress(char progress){
		char result = progress;
		try {
			OutputStream outputStream = new FileOutputStream(progressFile);
			outputStream.write(progress);
			outputStream.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
			result = 'F';
		}
		return result;
	}
	
	public boolean getOpenArena(){
		progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		textView = (TextView)findViewById(R.id.textView1);
		WifiManager.WifiLock wifilock; 
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE); 
		wifilock = manager.createWifiLock("DROID_ARENA"); 
		wifilock.acquire();
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DROID_ARENA");
		wakeLock.acquire();
		boolean result = false;
		char progress = getProgressFile();
		File zipFile;
		if (progress != 'D'){
			zipFile = download();
			progress = writeProgress('D');
		}
		else {
			zipFile = new File(openArenaTempStoragePath + "test.zip");
		}
		if (progress != 'Z'){
			unzip(zipFile);
			progress = writeProgress('Z');
		}
		percentageDone = 0;
		mHandler.post(new Runnable() {
            public void run() {
				textView.setText("Moving files and deleting temporary directories");
				progressBar.setProgress(percentageDone);
            }
        });
		if (progress != 'M'){
			moveFiles();
			progress = writeProgress('M');
		}
		percentageDone = 50;
		mHandler.post(new Runnable() {
            public void run() {
				textView.setText("Moving files and deleting temporary directories");
				progressBar.setProgress(percentageDone);
            }
        });
		File tempDirectory = new File(openArenaTempStoragePath);
		deleteDir(tempDirectory);
		percentageDone = 100;
		mHandler.post(new Runnable() {
            public void run() {
				textView.setText("Open Arena has been installed, press back to return to the menu");
				progressBar.setProgress(percentageDone);
            }
        });
		wifilock.release();
		wakeLock.release();
		return result;
	}
	
	public void deleteDir(File dir){
		File[] children = dir.listFiles();
		for (int i = 0; i < children.length; i++){
			File child = children[i];
			if (child.isDirectory()){
				deleteDir(child);
			}
			child.delete();
		}
		dir.delete();
	}
	
	public File download(){
		File openArenaZip = null;
		try{			
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			URL openArenaURL = new URL(openArenaDownloadPath);
			URLConnection connection = openArenaURL.openConnection();
			InputStream inputStream = connection.getInputStream();
			BufferedInputStream downloadStream = new BufferedInputStream(inputStream);
			ByteArrayBuffer buffer = new ByteArrayBuffer(bufferCapacity );
			int data = 0;
			int count = 0;
			percentageDone = 0;
			openArenaZip = new File(openArenaTempStoragePath + "test.zip");
			if (openArenaZip.exists()){
				Log.d("DROID_ARENA","" + openArenaZip.delete());
				openArenaZip = new File(openArenaTempStoragePath + "test.zip");
			}
			FileOutputStream fileWriter = new FileOutputStream(openArenaZip);
			Log.d("DROID_ARENA", percentageDone + "% done.");
			while ((data = downloadStream.read()) != -1){	//download data
				buffer.append((byte) data);
				if (count == percentageIncrements){
					count = -1;
					percentageDone ++;
					// Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
        					progressBar.setProgress(percentageDone);
                        }
                    });
					Log.d("DROID_ARENA", percentageDone + "% done.");

					fileWriter.write(buffer.toByteArray());
					buffer.clear();
				}
				//fileWriter.write((byte) data);
				count++;

				//Log.d("DROID_ARENA", "" + count);
			}
			Log.d("DROID_ARENA", "PROBLEM!?");

			//openArenaZip = new File(openArenaTempStoragePath + "test.zip");
			//FileOutputStream fileWriter = new FileOutputStream(openArenaZip);
			//fileWriter.write(buffer.toByteArray());
			fileWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return openArenaZip;
	}
	
	public boolean isNeededFile(String fileName){
		boolean result = false;
		String base = "openarena-0.8.1/baseoa/";
		if (fileName.equals("openarena-0.8.1/missionpack/"+"mp-pak0.pk3") || fileName.equals(base+"pak0.pk3") || fileName.equals(base+"pak1-maps.pk3") || fileName.equals(base+"pak2-players.pk3") 
				|| fileName.equals(base+"pak2-players-mature.pk3") || fileName.equals(base+"pak2-players-mature.pk3") || fileName.equals(base+"pak4-textures.pk3") 
				|| fileName.equals(base+"pak5-TA.pk3") || fileName.equals(base+"pak6-misc.pk3")){
			
			result = true;
		}
		return result;
	}
	
	public boolean unzip(File file){
		boolean result = false;
		try{
			FileInputStream fileInputStream = new FileInputStream(file); 
			ZipInputStream zipStream = new ZipInputStream(fileInputStream);
			ZipEntry zipContents = null; 
			ByteArrayBuffer buffer = new ByteArrayBuffer(bufferCapacity);
			
			while ((zipContents = zipStream.getNextEntry()) != null){ 	
		        if(zipContents.isDirectory()){
		        	if (!folderExists(openArenaTempStoragePath + zipContents.getName())){
		    			makeFolder(openArenaTempStoragePath + zipContents.getName());
		    		} 
		        }
		        else if (isNeededFile(zipContents.getName())){
		        	final String fileName = zipContents.getName();
		        	mHandler.post(new Runnable() {
		                public void run() {
		                	textView.setText("Extracting file " + fileName);
		                }
		            });
		        	int data = 0;
					int count = 0;
					int bytesSize = 1024;
					byte[] bytes = new byte[bytesSize];
					percentageDone = 0;
					percentageIncrements = zipContents.getSize()/100;
		        	FileOutputStream fileOutputStream = new FileOutputStream(openArenaTempStoragePath + zipContents.getName());
		        	while ((data = zipStream.read(bytes)) != -1){
		        		//buffer.append((byte) data);
		        		buffer.append(bytes, 0, data);
		        	//while ((data = zipStream.read()) != -1){
		        		//buffer.append((byte) data);
		        		//if (count == (bufferCapacity-1)){
		        		count += data;
		        		if (count >= percentageIncrements-(bytesSize+1)){//percentageIncrements-1025){
		        			fileOutputStream.write(buffer.toByteArray());
		        			buffer.clear();
		        			percentageDone++;
		        			mHandler.post(new Runnable() {
				                public void run() {
				                	progressBar.setProgress(percentageDone);
				                }
				            });
		        			count = -1;
							Log.d("DROID_ARENA", "writing unzipped data to file");
		        		}
		        	}
		        	if (count > 0){
	        			fileOutputStream.write(buffer.toByteArray());
	        			buffer.clear();
	        			count = -1;
						Log.d("DROID_ARENA", "finished unzipping file");
		        	}
		        	zipStream.closeEntry();
		        	fileOutputStream.close();
		        }		         
		      }
			Log.d("DROID_ARENA", "all files unzipped");
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
