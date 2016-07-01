package org.mdtp.evaluation.jmeter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JMeterWrapper {

	/**
	 * location of the JMeter bin folder
	 */
	private Process jmeterProcess;

	private String jmeterRootFolder;
	
	public JMeterWrapper(String jmeterRootFolder) {
		this.jmeterRootFolder = jmeterRootFolder;
	}


	/**
	 * Starts a load test and then returns immediately. To wait for the test to finish use {@link waitForLoadTestFinish}
	 * or poll {@link isLoadTestRunning}
	 * 
	 * @param config The test configuration
	 * @throws IOException if starting load fails
	 */
	public synchronized void startLoadTest(String pathToScript, Map<String,String> params) throws IOException {

		// check whether a loadTest is already running
		if (jmeterProcess != null) {
			throw new RuntimeException("An Jmeter Process is already running, can only run one process per wrapperinstance");
		}
		
		File script = new File(pathToScript);
		System.out.println("Script read rights: "+script.canRead());

		// create log file
		
		List<String> cmd = buildCmdLine(pathToScript, params);
		
		System.out.println("jemter start command: "+cmd);

		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(jmeterRootFolder));
		// output needs to be redirected
		pb.inheritIO();
		//pb.
		
		jmeterProcess = pb.start();

		final JMeterWrapper thisWrapper = this;

		// add a Thread that waits for the Process to terminate who then
		// notifies all other waiting Threads
		new Thread(new Runnable() {

			public void run() {
				try {
					jmeterProcess.waitFor();

				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				synchronized (thisWrapper) {
					jmeterProcess = null;
					thisWrapper.notifyAll();
				}
			}
		}).start();
	}
	
	private List<String> buildCmdLine(String pathToScript, Map<String,String> parameters) {
		List<String> cmd = new ArrayList<String>();

		cmd.add("bin/jmeter");
		//cmd.add("bin"+File.separator+"ApacheJMeter.jar");
		cmd.add("-n"); // JMeter in non-gui mode
		cmd.add("-t"); // load script field path
		cmd.add(pathToScript);

		// now add all the JMeter variables
		//cmd.add("-Jp_durationSeconds=" + config.getExperimentDuration());
		
		parameters.forEach((k,v) -> cmd.add("-J"+k+"="+v)); 
		

		return cmd;
	}

	/**
	 * Checks whether a load test is running at the moment.
	 * 
	 * @return <tt>true</tt> if running, <tt>false</tt> if not
	 */
	public boolean isLoadTestRunning() {
		return (jmeterProcess != null);
	}

	/**
	 * Waits for the current loadtest to finish. If no loadtest is running, the method returns immediately.
	 * 
	 * @throws InterruptedException if the Thread is interrupted
	 */
	public synchronized void waitForLoadTestFinish() throws InterruptedException {

		while (jmeterProcess != null) {
			this.wait();
		}
	}
}
