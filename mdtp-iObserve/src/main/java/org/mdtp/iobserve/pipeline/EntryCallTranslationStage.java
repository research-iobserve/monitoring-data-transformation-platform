package org.mdtp.iobserve.pipeline;

import java.util.List;
import java.util.Optional;

import mdm.api.core.EventSubTrace;
import mdm.api.http.SessionAwareEvent;

import org.iobserve.analysis.data.EntryCallEvent;
import org.iobserve.analysis.filter.TEntryCallSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.cta.api.core.Location;
import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.core.callables.MethodInvocation;
import rocks.cta.api.core.callables.NestingCallable;
import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

/**
 * This stage is responsible for generating EntryCallRecords from the events and traces of the underlying mdm.
 * Afterwards, the original iObserve stage {@link TEntryCallSequence} can be executed.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class EntryCallTranslationStage extends AbstractConsumerStage<SessionAwareEvent> {
	
	private static final Logger LOG = LoggerFactory.getLogger(EntryCallTranslationStage.class);

	/** output port for the gnerated entry call events. */
	private final OutputPort<EntryCallEvent> output = this.createOutputPort();

	@Override
	protected void execute(SessionAwareEvent event) {
		
		//we are only interested in events which have a single trace assigned
		if(event.getTriggeredSubTraces().size() == 1) {
			
			EventSubTrace trace = event.getTriggeredSubTraces().get(0);
			MethodInvocation rootInvocation = getRootInvocation(trace);
			
			Optional<String> host = event.getLocation().map(Location::getHost);
			
			if(host.isPresent()) {
				if(rootInvocation != null) {
					output.send(
							//TODO: correct class name and method signature
							new EntryCallEvent(
									rootInvocation.getTimestamp(),
									rootInvocation.getExitTime(), 
									rootInvocation.getSignature(),
									rootInvocation.getClassName().get(),
									event.getSessionID().get(),
									host.get()));
				} else {
					LOG.warn("Can't translate session aware event and it's assigned trace, because it has no valid MethodInvocation as root");
				}
			} else {
				LOG.warn("The session aware event does not have a host assigned, which is required for a correct translation");
			}
		}
		
	}

	/**
	 * @param trace the trace to scan
	 * @return the highest level method invocation found in the execution trace tree
	 */
	private MethodInvocation getRootInvocation(EventSubTrace trace) {
		Callable currentRoot = trace.getRoot();
		while(currentRoot != null && ! (currentRoot instanceof MethodInvocation)) {
			//make sure it only has a single child, otherwise there is no such root
			if(currentRoot instanceof NestingCallable) {
				List<Callable> children = ((NestingCallable)currentRoot).getCallees();
				if(children.size() != 1) {
					return null;
				} else {
					currentRoot = children.get(0);
				}
			} else {
				return null;
			}
		}
		return (MethodInvocation) currentRoot;
	}

	protected OutputPort<EntryCallEvent> getOutputPort() {
		return output;
	}
	
	

}
