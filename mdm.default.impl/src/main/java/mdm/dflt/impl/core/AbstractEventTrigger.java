package mdm.dflt.impl.core;

import java.util.Optional;

import mdm.api.core.EventTrigger;
import rocks.cta.dflt.impl.core.callables.AbstractTimedCallableImpl;
import rocks.cta.dflt.impl.core.callables.RemoteInvocationImpl;

/**
 * Default implementation for {@link EventTrigger}.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 * @param <E> the event type which may be triggered by this trigger.
 */
public abstract class AbstractEventTrigger<E extends AbstractEvent> extends AbstractTimedCallableImpl implements EventTrigger<E>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4128141468456504619L;
	
	/**
	 * The triggered event, may be null.
	 */
	private E triggeredEvent;

	@Override
	public Optional<E> getTriggeredEvent() {
		return Optional.ofNullable(triggeredEvent);
	}
	
	/**
	 * Sets the triggered event. Any previous assignment will be undone.
	 * @param event the event which was triggered by this trigger.
	 */
	public void setTriggeredEvent(E event) {
		if(triggeredEvent == event) {
			return; //avoid infinite recursion
		}
		if(triggeredEvent != null) {
			triggeredEvent.setTrigger(null);
		}
		triggeredEvent = event;
		if(event != null) {
			triggeredEvent.setTrigger(this);
		}
	}

	
	
	

}
