package org.mdtp.evaluation.jmeter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class ExperiementController {
	
	public static String JMETER_PATH_PROP = "jmeter-root";
	public static String MVN_PATH = "mvn-path";
	public static String USER_GENERATION_SCRIPT = "user-generation-script";
	public static String LOAD_SCRIPT = "load-script";
	
	private JMeterWrapper jmeter;
	private BroadleafManager broadleaf;
	
	private Properties config;
	
	public ExperiementController(Properties config) {
		this.config = config;
		jmeter = new JMeterWrapper(config.get(JMETER_PATH_PROP).toString());
		
		
		
	}
	
	public void startAndSetUpBroadleaf(String broadleafPath, String args) {
		broadleaf = new BroadleafManager(broadleafPath,config.get(MVN_PATH).toString());
		broadleaf.start(args);
		System.out.println("Starting user generation...");
		
		try {
			jmeter.startLoadTest(config.get(USER_GENERATION_SCRIPT).toString(), Collections.emptyMap());
			jmeter.waitForLoadTestFinish();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		System.out.println("System ready for testing!");
	}
	
	public void startloadTest(long durationSeconds, String userScript) {
		try {
			Map<String,String> params = new HashMap<>();
			params.put("p_durationSeconds", ""+durationSeconds);
			params.put("p_behaviour", userScript);
			jmeter.startLoadTest(config.get(LOAD_SCRIPT).toString(), params);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	

	public void waitForLoadTestFinish() {
		try {
			jmeter.waitForLoadTestFinish();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void terminateBroadLeaf() throws InterruptedException {
		System.out.println("----------------- Terminating Broadleaf ------------------------");
		broadleaf.terminate();
		broadleaf = null;
	}
	
	

}
