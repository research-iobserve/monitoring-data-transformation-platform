package org.mdtp.iobserve.pipeline;

import mdm.api.core.Event;
import mdm.api.deployment.EJBDeploymentEvent;
import mdm.api.deployment.ServletDeploymentEvent;

import org.iobserve.analysis.filter.DeploymentEventTransformation;
import org.iobserve.analysis.filter.UndeploymentEventTransformation;
import org.iobserve.common.record.IDeploymentRecord;
import org.iobserve.common.record.IUndeploymentRecord;

import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

/**
 * This pipeline stage converts MDM deployment events back to the corresponding Kieker records.
 * This allows the execution of the original {@link DeploymentEventTransformation} and  {@link UndeploymentEventTransformation}
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class DeploymentEventBackTranslationStage extends AbstractConsumerStage<Event> {

	/** output port for deployment events. */
	private final OutputPort<IDeploymentRecord> deploymentOutputPort = this.createOutputPort();
	/** output port for undeployment events. */
	private final OutputPort<IUndeploymentRecord> undeploymentOutputPort = this.createOutputPort();
	

	protected OutputPort<IDeploymentRecord> getDeploymentOutputPort() {
		return deploymentOutputPort;
	}

	protected OutputPort<IUndeploymentRecord> getUndeploymentOutputPort() {
		return undeploymentOutputPort;
	}
	
	@Override
	protected void execute(Event input) {
		
		if(input instanceof EJBDeploymentEvent) {
			translateEJBEvent((EJBDeploymentEvent) input);
		} else if(input instanceof ServletDeploymentEvent) {
			translateServletEvent((ServletDeploymentEvent) input);
		}
		//other event types are ignored
		
	}

	private void translateServletEvent(ServletDeploymentEvent input) {
		if(input instanceof mdm.api.deployment.DeployedEvent) {
			deploymentOutputPort.send(
					new org.iobserve.common.record.ServletDeployedEvent(
					input.getTimestamp(),
					input.getService().orElse(null),
					input.getContext().orElse(null),
					input.getDeploymentID().orElse(null)));
		} else if (input instanceof mdm.api.deployment.UndeployedEvent) {
			undeploymentOutputPort.send(
			 new org.iobserve.common.record.ServletUndeployedEvent(
					input.getTimestamp(),
					input.getService().orElse(null),
					input.getContext().orElse(null),
					input.getDeploymentID().orElse(null)));			
		} else {
			throw new IllegalArgumentException("Unexpected event type");
		}
	}

	private void translateEJBEvent(EJBDeploymentEvent input) {
		if(input instanceof mdm.api.deployment.DeployedEvent) {
			deploymentOutputPort.send(
			 new org.iobserve.common.record.EJBDeployedEvent(
					input.getTimestamp(),
					input.getContext().orElse(null),
					input.getDeploymentID().orElse(null)));
		} else if (input instanceof mdm.api.deployment.UndeployedEvent) {
			undeploymentOutputPort.send(
			 new org.iobserve.common.record.EJBUndeployedEvent(
					input.getTimestamp(),
					input.getContext().orElse(null),
					input.getDeploymentID().orElse(null)));			
		} else {
			throw new IllegalArgumentException("Unexpected event type");
		}
	}

}
