package mdm.dflt.impl.core;
import java.util.Optional;

import mdm.api.core.Event;
import mdm.api.core.EventSubTrace;
import mdm.api.core.UnmonitoredEvent;
import rocks.cta.dflt.impl.core.SubTraceImpl;
import rocks.cta.dflt.impl.core.TraceImpl;
import rocks.cta.dflt.impl.core.callables.AbstractCallableImpl;


/**
 * Default implementation for {@link EventSubTrace}.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class EventSubTraceImpl extends SubTraceImpl implements EventSubTrace {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8892058036975595828L;
	
	/**
	 * References the trigger of this event.
	 * Null is allowed for SubTraces which were directly called by other subtraces, not through events.
	 * For root SubTraces, {@link UnmonitoredEvent} should be used isntead.
	 */
	private AbstractEvent triggeringEvent;
	
	
	
	
	public EventSubTraceImpl() {
		super();
	}

	public EventSubTraceImpl(long subTraceId, SubTraceImpl parentSubTrace, TraceImpl containingTrace) {
		super(subTraceId, parentSubTrace, containingTrace);
	}

	/**
	 * Converts a normal CTA {@link SubTraceImpl} into an {@link EventSubTraceImpl}.
	 * @param trace the trace to be converted.
	 * @return the converted trace.
	 */
	public static EventSubTraceImpl createFromSubTrace(SubTraceImpl trace) {
		EventSubTraceImpl et = new EventSubTraceImpl(trace.getSubTraceId(), (SubTraceImpl)trace.getParent(), (TraceImpl)trace.getContainingTrace());
		et.setIdentifier(trace.getIdentifier().orElse(null));
		et.setLocation(trace.getLocation());
		et.setRoot((AbstractCallableImpl)trace.getRoot());
		return et;
	}

	/**
	 * Sets the event which triggered this trace, may be null.
	 * @param trigger the triggering event, may be null
	 */
	public void setTriggeringEvent(AbstractEvent trigger) {
		
		if(triggeringEvent == trigger) {
			return; //avoid infinite recursion
		}
		if(triggeringEvent != null) {
			triggeringEvent.removeTriggeredSubTrace(this);
		}
		triggeringEvent = trigger;
		if(triggeringEvent != null) {
			triggeringEvent.addTriggeredSubTrace(this);
		}
		
	}
	
	@Override
	public Optional<Event> getTriggeringEvent() {
		return Optional.ofNullable(triggeringEvent);
	}

}
