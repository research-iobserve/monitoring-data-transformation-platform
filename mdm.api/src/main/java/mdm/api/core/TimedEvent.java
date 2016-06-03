package mdm.api.core;


/**
 * Represents an event whose occurrence can be timed.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface TimedEvent extends Event {
	
	/**
	 * @return timestamp of the event occurrence in nano seconds
	 */
	long getTimestamp();

}
