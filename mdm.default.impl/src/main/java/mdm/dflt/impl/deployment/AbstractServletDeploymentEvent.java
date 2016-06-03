package mdm.dflt.impl.deployment;

import java.util.Optional;

import mdm.api.deployment.ServletDeploymentEvent;
import mdm.dflt.impl.core.AbstractTimedEvent;

/**
 * Abstract default implementation for {@link ServletDeploymentEvent}.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public abstract class AbstractServletDeploymentEvent extends AbstractTimedEvent implements ServletDeploymentEvent{
	
	private String service;
	private String context;
	private String deploymentID;
	
	
	public Optional<String> getService() {
		return Optional.ofNullable(service);
	}
	public void setService(String service) {
		this.service = service;
	}
	public Optional<String> getContext() {
		return Optional.ofNullable(context);
	}
	public void setContext(String context) {
		this.context = context;
	}
	public Optional<String> getDeploymentID() {
		return Optional.ofNullable(deploymentID);
	}
	public void setDeploymentID(String deploymentID) {
		this.deploymentID = deploymentID;
	}
	
	

}
