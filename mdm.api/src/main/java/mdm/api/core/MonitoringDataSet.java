package mdm.api.core;

import java.util.List;

import rocks.cta.api.core.Trace;

/**
 * 
 * The root element of an Monitoring Data Model instance.
 * It contains a set of all root events. Additionally, all Traces are stroed, but their root SubTraces should usually be referenced by the root events.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface MonitoringDataSet {
	
	/**
	 * @return the root events of this MDM instance. A root event is an event which has no observable trigger.
	 */
	List<Event> getRootEvents();
	/**
	 * @return all Traces of this MDM instance.
	 */
	List<Trace> getTraces();

}
