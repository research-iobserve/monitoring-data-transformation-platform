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


import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

import io.github.wessbas.kiekerExtensions.record.ServletEntryRecord;
import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import mdm.api.core.TimedEvent;
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
@Plugin(description = "Filter for extracting http info to a mdm instance", outputPorts = {}, configuration = {})
public final class WESSBASHttpInfoExtractionFilter extends AbstractTraceIDLookupFilter {

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
	public WESSBASHttpInfoExtractionFilter(final Configuration configuration, final IProjectContext projectContext) {
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

		if(object instanceof ServletEntryRecord) {
			
			ServletEntryRecord rec = (ServletEntryRecord) object;
			//scan for the event trace with this trace id
			Map<Long,Trace> idMap = getTraceIdMap(mdm);
			if(idMap.containsKey(rec.getTraceId())) {
				
				Trace trace = idMap.get(rec.getTraceId());
				
				//we expect the trace to not be malformed, therefore unchecked casts
				
				EventSubTraceImpl root = (EventSubTraceImpl) trace.getRoot();
				
				LocationImpl loc = new LocationImpl();
				loc.setHost(rec.getHost()+":"+rec.getPort());
				
				HTTPMethod method;
				switch(rec.getMethod()) {
				case "GET": method = HTTPMethod.GET; break;
				case "POST": method = HTTPMethod.POST; break;
				default: throw new RuntimeException("unhandled HTTP method: " + rec.getMethod());
				}

				
				String protocol = rec.getProtocol();
				String encoding = rec.getEncoding();
				if(encoding.equals("<no-encoding>")) {
					encoding = null;
				}
				Map<String,String[]> parameters = decodeParameters(rec.getQueryString());
				
				HTTPRequestReceivedEventImpl event = (HTTPRequestReceivedEventImpl) root.getTriggeringEvent().get();
				
				event.setLocation(loc);
				event.setRequestMethod(method);
				event.setURI(rec.getUri());
				event.setHTTPParameters(parameters);
				event.setEncoding(encoding);
				event.setProtocol(protocol);
				
				HTTPRequestProcessingImpl processing = (HTTPRequestProcessingImpl) root.getRoot();
				
				processing.setRequestMethod(method);
				processing.setUri(rec.getUri());
				
			}
		}
	}
	

	/**
	 * Adapted from
	 * http://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
	 * 
	 * @param queryString
	 * @return
	 */
	private Map<String, String[]> decodeParameters(String queryString) {
		if(queryString.equals("<no-query-string>")) {
			return new HashMap<>();
		}
		HashMap<String, List<String>> result = new HashMap<>();
		try {
			final String[] pairs = queryString.split("&");
			for (String pair : pairs) {
				final int idx = pair.indexOf("=");
				final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
				if (!result.containsKey(key)) {
					result.put(key, new LinkedList<String>());
				}
				final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
				result.get(key).add(value);
			}
		}catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		HashMap<String, String[]> arrayResult = new HashMap<>();
		for(Entry<String,List<String>> entry : result.entrySet()) {
			arrayResult.put(entry.getKey(), entry.getValue().toArray(new String[0]));
		}
		return arrayResult;
				
	}



	/**
	 * @param outputMdm the MDM whose HTTPRequestRecieved events will be updated using the ServletEntryRecords.
	 */
	public void setMonitoringDataSet(MonitoringDataSetImpl outputMdm) {
		mdm = outputMdm;
	}
}