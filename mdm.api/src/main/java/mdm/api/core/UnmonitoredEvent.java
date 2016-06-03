package mdm.api.core;

import java.util.Optional;

/**
 * Represents an event which could not be monitored (e.g. because it occurred outside of the monitored scope).
 * This is useful to be specified as root events for Traces which have no known triggers.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface UnmonitoredEvent extends Event{

	
	public default Optional<EventTrigger<?>> getTrigger() {
		return Optional.empty();
	}

}
