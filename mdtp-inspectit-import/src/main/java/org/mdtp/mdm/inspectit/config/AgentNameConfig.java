package org.mdtp.mdm.inspectit.config;


import java.io.IOException;
import java.util.stream.StreamSupport;

import org.mdtp.core.impl.DefaultConfigurationProperty;
import org.mdtp.mdm.inspectit.rest.InspectITRestClient;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;

/**
 * Configration Property for configuring an agent identifier.
 * @author Jonas Kunz
 *
 */
public class AgentNameConfig extends DefaultConfigurationProperty<String> {

	public AgentNameConfig(String name, String description) {
		super(name, description, String.class);
	}

	/**
	 * @param hostWithPort the host and port of the CMR
	 * @return true, if such an agent exists and is unique at the given CMR
	 */
	public boolean isAgentExistingAndUnique(String hostWithPort) {
		
		String refrenceName = getValue().orElse("");
		if(refrenceName.equals("")) {
			return false;
		} else {
			InspectITRestClient testClient = new InspectITRestClient(hostWithPort);			
			try {
				long numEqualAgents =
				StreamSupport.stream(testClient.fetchAllAgents().spliterator(),false)
				.map(PlatformIdent::getAgentName)
				.filter((n) -> n.equalsIgnoreCase(refrenceName))
				.count();
				return numEqualAgents == 1;
			}catch(IOException e) {
				return false;
			} finally {
				testClient.close();
			}			
		}
		
	}
	

}
