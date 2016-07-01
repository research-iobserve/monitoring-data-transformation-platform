package org.mdtp.evaluation.jmeter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;


public class Main {
	
	public static final String MEASUREMENT_DURATIONS = "durationsSeconds";
	public static final String LOAD_BEHAVIOURS = "load-behaviours";
	public static final String MONITORING_TOOL = "monitoring-tool";
	public static final String BROADLEAF_NORMAL_PATH = "broadleaf-normal-path";
	public static final String BROADLEAF_KIEKER_PATH = "broadleaf-kieker-path";
	public static final String INSPECTIT_AGENT_ARGS = "agent-args";

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		if(args.length != 1) {
			System.out.println("Expecting .proeprties file as command line argument!");
			return;
		}
		Properties props = new Properties();
		props.load(new FileReader(new File(args[0])));
		
		String agentArgs = props.getProperty(INSPECTIT_AGENT_ARGS);
		
		
		ExperiementController controller = new ExperiementController(props);
		
		String[] durations = props.getProperty(MEASUREMENT_DURATIONS).split(";");
		String[] behaviours = props.getProperty(LOAD_BEHAVIOURS).split(";");
		String[] tools = props.getProperty(MONITORING_TOOL).split(";");
		

		for(String tool : tools) {
			String broadleafPath = "";
			String startupArgs ="";
			IMonitoringController monitoring = null;
			if(tool.equalsIgnoreCase("inspectIT")) {
				broadleafPath = props.getProperty(BROADLEAF_NORMAL_PATH);
				startupArgs = agentArgs;
				monitoring = new InspectITMonitoringController(props);
			} else if(tool.startsWith("kieker")){
				broadleafPath = props.getProperty(BROADLEAF_KIEKER_PATH);
				startupArgs = "";			
				monitoring = new KiekerMonitoringController(props);
				if(tool.contains("execution")) {
					((KiekerMonitoringController)monitoring).setExecutionBasedRecording(true);
				} else {
					((KiekerMonitoringController)monitoring).setExecutionBasedRecording(false);					
				}
				
			}
			for(String behaviour : behaviours) {				
				for(String dur : durations) {
					String measurementPath = "measurment/"+tool+"/"+FilenameUtils.getBaseName(behaviour)+"/"+dur;
					System.out.println("Starting measurement "+measurementPath);
							
					int durS = Integer.parseInt(dur);
					
					System.out.println("setting up environment...");
					controller.startAndSetUpBroadleaf(broadleafPath,startupArgs);
					System.out.println("starting loadscript");
					controller.startloadTest(durS+50+60+60, behaviour);
					System.out.println("waiting for ramp-up");
					Thread.sleep((50+60)*1000);
					System.out.println("starting monitoring...");
					
					monitoring.startMonitoring();
					
					System.out.println("waiting for measurement end..");
					Thread.sleep(durS*1000L);
					
					System.out.println("Stopping monitoring....");
					monitoring.stopMonitoring();

					System.out.println("Waiting for load test end");
					controller.waitForLoadTestFinish();

					System.out.println("shutting down environment");
					controller.terminateBroadLeaf();

					System.out.println("copying data...");
					monitoring.copyDataAndCleanup(new File(measurementPath));
					System.out.println("done.");
				}
			}
		}
		
		
	
		
	}

}
