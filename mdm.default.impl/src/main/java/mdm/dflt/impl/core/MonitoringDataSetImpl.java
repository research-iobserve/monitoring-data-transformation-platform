package mdm.dflt.impl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import rocks.cta.api.core.SubTrace;
import rocks.cta.api.core.Trace;
import rocks.cta.dflt.impl.core.TraceImpl;
import mdm.api.core.Event;
import mdm.api.core.MonitoringDataSet;

/**
 * Default implementation for {@link MonitoringDataSet}, representing the root of Monitoring Data Model instance.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph Heger
 *
 */
public class MonitoringDataSetImpl implements MonitoringDataSet {

	/**
	 * The collection of root events stored in this MDM instance.
	 */
	private List<Event> rootEvents = new ArrayList<>();
	

	
	@Override
	public List<Event> getRootEvents() {
		return Collections.unmodifiableList(rootEvents);
	}

	@Override
	public List<Trace> getTraces() {
		
		//the traces are implicitly referenced by the eventsubtraces of the root events.
		List<Trace> allTraces =
		rootEvents.stream()
		.flatMap((event) -> event.getTriggeredSubTraces().stream())
		.map(SubTrace::getContainingTrace)
		.distinct()
		.collect(Collectors.toList());
		
		return Collections.unmodifiableList(allTraces);
	}
	
	/**
	 * Adds a root event to this MDM instance.
	 * 
	 * @param event the root event to add, meaning that it must not have a trigger
	 */
	public void addRootEvent(AbstractEvent event) {
		if(event.getTrigger().isPresent()) {
			throw new IllegalArgumentException("The given event is not a root event, as it has a trigger!");
		}
		if(!rootEvents.contains(event)) {
			rootEvents.add(event);
		}
	}

	/**
	 * Removes the given event from the set of root events.
	 * @param event the root event to remove.
	 */
	public void removeRootEvent(AbstractEvent event) {
		rootEvents.remove(event);
	}
}
