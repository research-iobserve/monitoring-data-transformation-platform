package mdm.api.core;

import java.util.Optional;

import rocks.cta.api.core.callables.TimedCallable;

/**
 * Special callable type which represents the triggering of an event.
 * 
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 * @param <E> teh type of the events which can be triggered by this trigger.
 */
public interface EventTrigger<E extends Event> extends TimedCallable{
	
	/**
	 * @return the triggered event, if the event was triggered within the boundaries of the observable system.
	 */
	Optional<E> getTriggeredEvent();

}
