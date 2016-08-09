package org.mdtp.mdm.inspectit.config;


import java.io.IOException;

import org.mdtp.core.impl.DefaultConfigurationProperty;
import org.mdtp.mdm.inspectit.rest.InspectITRestClient;

/**
 * Configuration property for defining the connection to the CMR.
 * 
 * @author Jonas Kunz
 *
 */
public class CmrHostConfig extends DefaultConfigurationProperty<String> {

	public CmrHostConfig(String name, String description) {
		super(name, description, String.class);
	}

	/**
	 * @return true, if a CMR is accessible under the configured host and port
	 */
	public boolean isHostAccessible() {
		String hostWithPort = getValue().orElse("");
		if(hostWithPort.equals("")) {
			return false;
		} else {
			InspectITRestClient testClient = new InspectITRestClient(hostWithPort);			
			try {
				testClient.fetchAllAgents().iterator().next();
				return true;
			}catch(IOException e) {
				return false;
			} finally {
				testClient.close();
			}
		}
	}
	

}
