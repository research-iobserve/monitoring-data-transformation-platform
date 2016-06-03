package mdm.api.core;

import java.util.List;
import java.util.Optional;

import rocks.cta.api.core.Location;

/**
 * Base interface representing an occuring event.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface Event {
	
	/**
	 * @return the SubTraces which perform the processing of this event.
	 */
	List<EventSubTrace> getTriggeredSubTraces();

	/**
	 * @return the location, where this event was encountered.
	 */
	Optional<Location> getLocation();
	
	/**
	 * The trigger is a callable which triggered this event.
	 * It is null, if this event is a root event.
	 * 
	 * @return the EventTrigger which triggered this event. 
	 */
	Optional<EventTrigger<?>> getTrigger();
	
}
