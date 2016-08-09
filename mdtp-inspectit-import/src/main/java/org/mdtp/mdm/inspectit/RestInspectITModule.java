package org.mdtp.mdm.inspectit;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import mdm.api.core.MonitoringDataSet;

import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.ImportModule;
import org.mdtp.mdm.inspectit.config.AgentNameConfig;
import org.mdtp.mdm.inspectit.config.CmrHostConfig;
import org.mdtp.mdm.inspectit.rest.InspectITRestClient;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * Module for importing the Traces stored in the buffer of a CMR repository.
 * Uses the version independent REST-API of inpsectIT.
 * 
 * @author Jonas Kunz
 *
 */
public class RestInspectITModule implements ImportModule {

	/**
	 * The location of the cmr to connect to.
	 */
	private CmrHostConfig cmrConfig = new CmrHostConfig("cmr", "Location of the CMR to use in the form of host:port.");

	/**
	 * The name of the agent connect to the CMR whose data shall be imported.
	 */
	private AgentNameConfig agentName = new AgentNameConfig("agentName", "The name of the inspectIT-agent to use.");

	private List<ConfigurationProperty<String>> props = Arrays.asList(cmrConfig, agentName);

	@Override
	public List<? extends ConfigurationProperty<?>> getConfiguration() {
		return props;
	}

	public CmrHostConfig getCmrConfig() {
		return cmrConfig;
	}

	public AgentNameConfig getAgentNameConfig() {
		return agentName;
	}

	@Override
	public void validateConfiguration(ErrorBuffer errors) {

		if (!cmrConfig.isHostAccessible()) {
			errors.addError("The CMR at \"" + cmrConfig.getValue().orElse("") + "\" is not accessible!");
		} else {
			if (!agentName.isAgentExistingAndUnique(cmrConfig.getValue().get())) {
				errors.addError("The agent with the name \"" + agentName.getValue().orElse("") + "\" is either not connected or exists multiple times.");
			}
		}

	}

	@Override
	public MonitoringDataSet importMonitoringDataModel() {

		InspectITRestClient fetcher = new InspectITRestClient(cmrConfig.getValue().get());


		PlatformIdent agent;

		Map<Long, MethodIdent> methods = new HashMap<>();
		try {
			agent = StreamSupport.stream(fetcher.fetchAllAgents().spliterator(), false).filter((a) -> a.getAgentName().equalsIgnoreCase(agentName.getValue().get()))
					.findFirst().get();
		
			for (MethodIdent method : fetcher.fetchAllMethods(agent.getId())) {
				methods.put(method.getId(), method);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Iterable<InvocationSequenceData> invocationSequences = fetcher.fetchAll(agent.getId());
		
		InvocationSequencesTranslator translator = new InvocationSequencesTranslator();
		
		return translator.translate(methods, agent, invocationSequences);
	}
	
}
