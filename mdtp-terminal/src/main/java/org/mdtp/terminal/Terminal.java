package org.mdtp.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import mdm.api.core.MonitoringDataSet;

/**
 * Terminal class. Uses System.in and System.out.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class Terminal {

	/**
	 * The MDM which is currently held in memory.
	 */
	private MonitoringDataSet currentMonitoringDataModel;
	
	/**
	 * Buffered reader for reading user input.
	 */
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	public void printf(String str, Object... args) {
		System.out.printf(str, args);
	}

	public void printfln(String str, Object... args) {
		printf(str+"\n", args);
	}
	
	public String readString(){
		try {
			return br.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Optional<Integer> readInt() {
		String inStr = readString();
		try{
			return Optional.of(Integer.parseInt(inStr));
		} catch(NumberFormatException ex) {
			return Optional.empty();
		}
	}
	
	
	
	public Optional<MonitoringDataSet> getCurrentMonitoringDataModel() {
		return Optional.ofNullable(currentMonitoringDataModel);
	}

	public void setCurrentMonitoringDataModel(MonitoringDataSet currentMonitoringDataModel) {
		this.currentMonitoringDataModel = currentMonitoringDataModel;
	}

	public boolean readYesNo() {
		String result;
		do{
			printf("(y/n): ");
			result = readString();
		} while(!result.equalsIgnoreCase("y") && !result.equalsIgnoreCase("n"));
		return result.equalsIgnoreCase("y");
	}
	
	
	
}
