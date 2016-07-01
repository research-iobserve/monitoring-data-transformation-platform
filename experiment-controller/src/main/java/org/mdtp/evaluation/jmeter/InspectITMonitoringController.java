package org.mdtp.evaluation.jmeter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.management.RuntimeErrorException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.mdtp.mdm.inspectit.rest.InspectITRestClient;

public class InspectITMonitoringController implements IMonitoringController{
	
	public static final String CMR_STORAGE_FOLDER = "cmr-storage-dir";
	public static final String CMR_CONNECTION = "cmr-connection";
	
	
	private String inputDir;
	
	private InspectITRestClient client;
	
	private String storageID;
	
	//private static final String kieker output-path;
	
	public InspectITMonitoringController(Properties config){
		
		inputDir = config.getProperty(CMR_STORAGE_FOLDER);
		client = new InspectITRestClient(config.getProperty(CMR_CONNECTION));
	}
	
	public void startMonitoring() {
		try {
			storageID = client.createStorage("temp-measure-storage");
			Thread.sleep(500);
			client.startRecording(storageID);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	


	
	public void stopMonitoring() {
		try {
			client.stopRecording();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void copyDataAndCleanup(File targetFolder) {
		try {
			if(targetFolder.exists()) {
				FileUtils.cleanDirectory(targetFolder);
			}
			File outDir = new File(targetFolder.getAbsolutePath()+"/"+storageID);
			FileUtils.forceMkdir(outDir);
			FileUtils.copyDirectory(new File(inputDir+"/"+storageID), outDir);
			client.deleteStorage(storageID);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
