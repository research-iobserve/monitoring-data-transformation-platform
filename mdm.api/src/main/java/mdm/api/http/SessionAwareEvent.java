package mdm.api.http;

import java.util.Optional;

import mdm.api.core.Event;

/**
 * 
 * Abstract event type which represent events which might have knowledge of a user session they belong to.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface SessionAwareEvent extends Event {
	/**
	 * @return an unique identifier for the session
	 */
	Optional<String> getSessionID();
}
