package mdm.dflt.impl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import rocks.cta.api.core.Location;
import rocks.cta.dflt.impl.core.LocationImpl;
import mdm.api.core.Event;
import mdm.api.core.EventSubTrace;
import mdm.api.core.EventTrigger;

/**
 * Abstract default implementation for the core functionality of {@link Event}.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public abstract class AbstractEvent implements Event{
	
	/**
	 * The collection of all SubTraces which are triggered by this event.
	 */
	List<EventSubTraceImpl> triggeredTraces = new ArrayList<>();
	
	/**
	 * The location where this event occured, may be null.
	 */
	LocationImpl location;
	
	/**
	 * The trigger which triggered this event. If it is null, this event is a root event.
	 */
	AbstractEventTrigger<? extends AbstractEvent> trigger;

	@Override
	public List<EventSubTrace> getTriggeredSubTraces() {
		return Collections.unmodifiableList(triggeredTraces);
	}
	
	
	/**
	 * Adds a EventSubTrace to the set of triggered SubTraces.
	 * If the given subtrace was previously assigned to a different event, this assignment is removed first.
	 * 
	 * @param trace the SubTrace to assign to this event
	 */
	public void addTriggeredSubTrace(EventSubTraceImpl trace) {
		
		if(triggeredTraces.contains(trace)) {
			return; //avoid infinite recursion
		}
		triggeredTraces.add(trace);
		//keep consistency
		if(trace != null) {
			trace.setTriggeringEvent(this);
		} 
	}
	
	/**
	 * Removed the given SubTrace from the set of triggered SubTraces.
	 * @param trace the trace to remove
	 */
	public void removeTriggeredSubTrace(EventSubTraceImpl trace) {
		
		if(!triggeredTraces.contains(trace)) {
			return; //avoid infinite recursion
		}
		triggeredTraces.remove(trace);
		trace.setTriggeringEvent(null);
	}

	@Override
	public Optional<Location> getLocation() {
		return Optional.ofNullable(location);
	}
	
	
	/**
	 * Sets the location at which this event occurred. May be set to null.
	 * @param loc
	 */
	public void setLocation(LocationImpl loc) {
		this.location = loc;
	}	

	@Override
	public Optional<EventTrigger<?>> getTrigger() {
		return Optional.ofNullable(trigger);
	}
	
	@SuppressWarnings("unchecked") //required for generic implementation
	public <E extends AbstractEvent> void setTrigger(AbstractEventTrigger<E> newTrigger) {
		if(newTrigger == trigger) {
			return; //avoid infinite recursion
		}
		if(trigger != null) {
			trigger.setTriggeredEvent(null);
		}
		trigger = newTrigger;
		//keep consistency
		if(newTrigger != null) {
			newTrigger.setTriggeredEvent((E)this); 
		}
	}
	
	
	

}
