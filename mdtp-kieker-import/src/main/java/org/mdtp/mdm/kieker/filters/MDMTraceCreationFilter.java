/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package org.mdtp.mdm.kieker.filters;


import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.flow.trace.TraceMetadata;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import mdm.dflt.impl.core.AbstractEvent;
import mdm.dflt.impl.core.EventSubTraceImpl;
import mdm.dflt.impl.core.MonitoringDataSetImpl;
import mdm.dflt.impl.core.UnmonitoredEventImpl;
import mdm.dflt.impl.http.HTTPRequestReceivedEventImpl;
import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.core.callables.MethodInvocation;
import rocks.cta.api.core.callables.TimedCallable;
import rocks.cta.dflt.impl.core.SubTraceImpl;
import rocks.cta.dflt.impl.core.TraceImpl;
import rocks.cta.dflt.impl.core.callables.HTTPRequestProcessingImpl;
import rocks.cta.dflt.impl.tranformer.DefaultCTATransformer;

@Plugin(description = "A filter to convert Kieker traces to MDM events and Traces", outputPorts = {}, configuration = {})
public final class MDMTraceCreationFilter extends AbstractFilterPlugin {

	/** The name of the input port for incoming events. */
	public static final String INPUT_PORT_NAME_EVENTS = "receivedEvents";

	public static final String KIEKER_TRACE_ID_KEY = "KIEKER_TRACE_ID";

	/**
	 * The Default CTA transfomrer is used to transform Kieker specific traces into MDM default implementation traces.
	 */
	private DefaultCTATransformer transformer;
	
	
	/**
	 * The resulting MDM events and traces wil lbe written into this mdm isntance.
	 */
	private MonitoringDataSetImpl mdm;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param configuration
	 *            The configuration for this component.
	 * @param projectContext
	 *            The project context for this component.
	 */
	public MDMTraceCreationFilter(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
		transformer = new DefaultCTATransformer();
	}

	@Override
	public final void terminate(final boolean error) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();
		// We reverse the if-decisions within the constructor.
		return configuration;
	}

	/**
	 * This method converts Kieker's trace object into an MDM trace.
	 * 
	 * @param object
	 *            The incoming record.
	 */
	@InputPort(name = INPUT_PORT_NAME_EVENTS, description = "Receives incoming objects to be logged and forwarded", eventTypes = { Object.class })
	public final void inputEvent(final Object object) {
		// ------------------------------------------------------Performance fix for testing purposes---------------------------------
		if (mdm.getRootEvents().size() > 1000) {
			//return;
		}
		
		if (object instanceof MessageTrace) {
			MessageTrace messageTrace = (MessageTrace) object;
			org.diagnoseit.spike.kieker.trace.impl.TraceImpl kiekerSPecificTrace = org.diagnoseit.spike.kieker.trace.impl.TraceImpl.createCallableTrace(messageTrace);
			
			
			TraceImpl trace = transformer.transform(kiekerSPecificTrace);

			//Traces get their Kieker Id as identifier fo further use
			trace.setIdentifier(messageTrace.getTraceId());
			
			// convert the root trace to an Event-Trace
			EventSubTraceImpl rootSubTrace = EventSubTraceImpl.createFromSubTrace((SubTraceImpl) trace.getRoot());
			trace.setRoot(rootSubTrace);

			AbstractEvent rootEvent = null;

			// we assume it was an HTTP Request if it has a session-id
			if (messageTrace.getSessionId() != null && !messageTrace.getSessionId().equals(TraceMetadata.NO_SESSION_ID)) {
				
				
				
				HTTPRequestReceivedEventImpl event = new HTTPRequestReceivedEventImpl();
				event.setSessionID(messageTrace.getSessionId());
				event.setTimestamp(messageTrace.getStartTimestamp());
				rootEvent = event;
				
				//wrap the subtrace root in a Http-Processing Callable
				HTTPRequestProcessingImpl reqProcessing = new HTTPRequestProcessingImpl();
				Callable oldRoot = rootSubTrace.getRoot();
				Callable kiekerRoot = kiekerSPecificTrace.getRoot().getRoot();
				
				reqProcessing.setTimestamp(oldRoot.getTimestamp());
				if(oldRoot instanceof TimedCallable) {
					reqProcessing.setResponseTime(((TimedCallable) oldRoot).getResponseTime());
				}
				
				reqProcessing.addCallee(oldRoot);
				rootSubTrace.setRoot(reqProcessing);
				
			} else {
				
				//unknown trigger, use Unmonitored event
				rootEvent = new UnmonitoredEventImpl();
			}

			rootEvent.addTriggeredSubTrace(rootSubTrace);
			synchronized (mdm) {
				mdm.addRootEvent(rootEvent);
			}

		}
	}

	/**
	 * @param outputMdm the MDM instance into which the generated events and traces will be drained
	 */
	public void setMonitoringDataSet(MonitoringDataSetImpl outputMdm) {
		mdm = outputMdm;
	}
}