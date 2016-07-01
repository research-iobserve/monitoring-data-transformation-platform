package org.mdtp.evaluation.jmeter;

import java.io.File;

public interface IMonitoringController {
	
	void startMonitoring();
	void stopMonitoring();
	void copyDataAndCleanup(File targetFolder);

}
