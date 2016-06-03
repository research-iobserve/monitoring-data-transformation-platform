package org.mdtp.iobserve.pipeline;

import mdm.api.core.Event;
import mdm.api.http.SessionAwareEvent;
import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

/**
 * 
 * This stage filters out {@link SessionAwareEvent}s and passes them to a separate output port.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class MDMEventSwitchStage extends AbstractConsumerStage<Event>{

	/** output port for deployment events. */
	private final OutputPort<SessionAwareEvent> sessionAwareEventsOutputPort = this.createOutputPort();
	/** output port for undeployment events. */
	private final OutputPort<Event> otherEventsOutputPort = this.createOutputPort();
	
	
	@Override
	protected void execute(Event inEvent) {
		if(inEvent instanceof SessionAwareEvent) {
			sessionAwareEventsOutputPort.send((SessionAwareEvent) inEvent);
		} else {
			otherEventsOutputPort.send(inEvent);
		}
		
	}


	protected OutputPort<SessionAwareEvent> getSessionAwareEventsOutputPort() {
		return sessionAwareEventsOutputPort;
	}


	protected OutputPort<Event> getOtherEventsOutputPort() {
		return otherEventsOutputPort;
	}
	
	
	
	

}
