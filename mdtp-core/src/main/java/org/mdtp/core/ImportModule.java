package org.mdtp.core;

import mdm.api.core.MonitoringDataSet;

/**
 * Base interface for all modules which impoprt monitoring tool specific monitoring data 
 * and transform into into a Monitoring Data Model instance.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface ImportModule extends Configurable {
	
	/**
	 * Imports the Monitoring Data Model based on the configuration of this module.
	 * It is expected that the configuration is correct, meaning that a call to 
	 * {@link ImportModule#validateConfiguration(ErrorBuffer)} results in no errors.
	 * @return the imported Monitoring Data Model
	 */
	MonitoringDataSet importMonitoringDataModel();

}
