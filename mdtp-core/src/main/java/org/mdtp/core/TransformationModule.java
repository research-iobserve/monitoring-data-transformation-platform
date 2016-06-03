package org.mdtp.core;

import mdm.api.core.MonitoringDataSet;

/**
 * Base interface for any platform Module performing analysis and transformation of
 * Monitoring Data Models into arbitary data.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface TransformationModule extends Configurable{
	

	
	/**
	 * Executes the tasks of this module based on the configuration.
	 * It is expected that the configuration is correct, meaning that a call to 
	 * {@link ImportModule#validateConfiguration(ErrorBuffer)} results in no errors.
	 * 
	 * @param monitoringData the MDM instance to be analyzed
	 */
	void execute(MonitoringDataSet monitoringData);

}
