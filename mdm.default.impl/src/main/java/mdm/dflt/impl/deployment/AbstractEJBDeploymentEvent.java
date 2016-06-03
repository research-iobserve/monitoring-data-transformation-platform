package mdm.dflt.impl.deployment;

import java.util.Optional;

import mdm.api.deployment.EJBDeploymentEvent;
import mdm.dflt.impl.core.AbstractTimedEvent;

/**
 * Abstract default implementation for {@link EJBDeploymentEvent}.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public abstract class AbstractEJBDeploymentEvent extends AbstractTimedEvent implements EJBDeploymentEvent{
	
	private String context;
	private String deploymentID;
	
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
