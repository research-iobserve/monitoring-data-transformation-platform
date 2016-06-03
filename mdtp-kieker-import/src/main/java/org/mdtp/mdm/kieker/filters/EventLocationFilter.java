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


import java.util.HashMap;
import java.util.Map;

import io.github.wessbas.kiekerExtensions.record.ServletEntryRecord;
import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.flow.trace.TraceMetadata;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import mdm.api.core.Event;
import mdm.dflt.impl.core.AbstractEvent;
import mdm.dflt.impl.core.EventSubTraceImpl;
import mdm.dflt.impl.core.MonitoringDataSetImpl;
import mdm.dflt.impl.http.HTTPRequestReceivedEventImpl;
import rocks.cta.api.core.SubTrace;
import rocks.cta.api.core.Trace;
import rocks.cta.api.core.callables.HTTPMethod;
import rocks.cta.dflt.impl.core.LocationImpl;
import rocks.cta.dflt.impl.core.SubTraceImpl;
import rocks.cta.dflt.impl.core.TraceImpl;
import rocks.cta.dflt.impl.core.callables.HTTPRequestProcessingImpl;
import rocks.cta.dflt.impl.tranformer.DefaultCTATransformer;

/**
 * 
 * This filter is responsible for extracting the additional HTTP info monitored by the WESSBAS kieker extension into the corresponding HTTP events.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
@Plugin(description = "Filter for extracting the location of triggering events from the trace meta data", outputPorts = {}, configuration = {})
public final class EventLocationFilter extends AbstractTraceIDLookupFilter {

	/** The name of the input port for incoming events. */
	public static final String INPUT_PORT_NAME_EVENTS = "receivedEvents";

	public static final String KIEKER_TRACE_ID_KEY = "KIEKER_TRACE_ID";

	/**
	 * The mdm to update.
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
	public EventLocationFilter(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
	}
	
	

	@Override
	public boolean init() {
		return super.init();
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
		return configuration;
	}
	
	
	/**
	 * process a single incoming record.
	 * @param object the incoming records
	 */
	@InputPort(name = INPUT_PORT_NAME_EVENTS, description = "Receives incoming objects to be logged and forwarded", eventTypes = { Object.class })
	public final void inputEvent(final Object object) {

		if(object instanceof TraceMetadata) {
			
			TraceMetadata tm = (TraceMetadata) object;
			//scan for the event trace with this trace id
			Map<Long,Trace> idMap = getTraceIdMap(mdm);
			if(idMap.containsKey(tm.getTraceId())) {
				
				Trace trace = idMap.get(tm.getTraceId());
				
				if(trace.getRoot() instanceof EventSubTraceImpl) {
					EventSubTraceImpl root = (EventSubTraceImpl) trace.getRoot();
					if(root.getTriggeringEvent().isPresent()){
						Event rootEvent = root.getTriggeringEvent().get();
						if(rootEvent instanceof AbstractEvent) {
							AbstractEvent ae = (AbstractEvent) rootEvent;
							if(!ae.getLocation().isPresent()) {
								
								LocationImpl loc = new LocationImpl();
								loc.setHost(tm.getHostname());								
								ae.setLocation(loc);
							}
						}
					}
				}
				
			}
		}
	}
	

	/**
	 * @param outputMdm the MDM whose HTTPRequestRecieved events will be updated using the ServletEntryRecords.
	 */
	public void setMonitoringDataSet(MonitoringDataSetImpl outputMdm) {
		mdm = outputMdm;
	}
}