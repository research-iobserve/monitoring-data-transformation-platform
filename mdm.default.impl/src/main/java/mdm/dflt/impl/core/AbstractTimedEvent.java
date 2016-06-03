package mdm.dflt.impl.core;

import mdm.api.core.TimedEvent;

/**
 * Abstract basic default type for {@link TimedEvent}
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class AbstractTimedEvent extends AbstractEvent implements TimedEvent {

	/**
	 * The timestamp when this event occurred, in nanoseconds.
	 */
	private long timestamp;

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp when this event occurred.
	 * 
	 * @param timestamp the timestamp in nanoseconds.
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
}
