package mdm.api.core;

import java.util.Optional;

import rocks.cta.api.core.SubTrace;

/**
 * Special type of SubTrace which was triggered by an event.
 * Therefore, EventSubTraces rerpesenting the processing of events.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface EventSubTrace extends SubTrace {
	
	/**
	 * Returns the Event which triggered this execution.
	 * May be null for SubTraces which were directly called by other subtraces, not through events.
	 * For root SubTraces, {@link UnmonitoredEvent} should be returned instead.
	 * 
	 * @return the event which triggered this SubTrace.
	 */
	Optional<Event> getTriggeringEvent();

}
