package org.mdtp.evaluation.jmeter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class KiekerMonitoringController implements IMonitoringController{
	
	public static final String KIEKER_MEASUREMENT_FOLDER = "kieker-output";
	
	private static final String TOGGLE_MONITORING_URL = "/toggle-measurement";
	
	private String inputDir;
	
	private String recordingMode = "event";
	
	private CloseableHttpClient client;
	//private static final String kieker output-path;
	
	public KiekerMonitoringController(Properties config){
		
		inputDir = config.getProperty(KIEKER_MEASUREMENT_FOLDER);
		
		HttpClientBuilder clientFactory = HttpClients.custom();		
		client = clientFactory.build();
	}
	
	public void startMonitoring() {
		Map<String,String> params = new HashMap<>();
		params.put("state", "ENABLED");
		params.put("mode", recordingMode);
		performGet(TOGGLE_MONITORING_URL, params);
	}
	
	public void setExecutionBasedRecording(boolean set) {
		if(set) {
			recordingMode = "execution";
		} else {
			recordingMode = "event";
		}
	}
	
	
	
	private void performGet(String path, Map<String,String> params) {
		URI requestTarget;
		try {
			URIBuilder builder = new URIBuilder("http://localhost:8080" + path);
			for (Entry<String, String> e: params.entrySet()) {
				builder.addParameter(e.getKey(), e.getValue());
			}
			requestTarget = builder.build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		HttpGet getReq = new HttpGet(requestTarget);
		HttpClientContext context = HttpClientContext.create();
		HttpResponse response;
		try {
			response = client.execute(getReq, context);
			String response2 = new BasicResponseHandler()
			.handleResponse(response);
			System.out.println("Localhost Responded with: "+response2);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	

	
	public void stopMonitoring() {
		Map<String,String> params = new HashMap<>();
		params.put("state", "DISABLED");
		performGet(TOGGLE_MONITORING_URL, params);
	}
	
	public void copyDataAndCleanup(File targetFolder) {
		try {
			FileUtils.forceMkdir(targetFolder);
			FileUtils.cleanDirectory(targetFolder);
			FileUtils.copyDirectory(new File(inputDir), targetFolder);
			FileUtils.cleanDirectory(new File(inputDir));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
