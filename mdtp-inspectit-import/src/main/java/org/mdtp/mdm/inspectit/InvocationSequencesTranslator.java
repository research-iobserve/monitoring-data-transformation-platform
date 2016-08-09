package org.mdtp.mdm.inspectit;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import mdm.api.core.MonitoringDataSet;
import mdm.dflt.impl.core.AbstractEvent;
import mdm.dflt.impl.core.EventSubTraceImpl;
import mdm.dflt.impl.core.MonitoringDataSetImpl;
import mdm.dflt.impl.core.UnmonitoredEventImpl;
import mdm.dflt.impl.http.HTTPRequestReceivedEventImpl;

import org.mdtp.mdm.inspectit.impl.IITTraceImpl;

import rocks.cta.api.core.Trace;
import rocks.cta.api.core.callables.HTTPMethod;
import rocks.cta.dflt.impl.core.LocationImpl;
import rocks.cta.dflt.impl.core.SubTraceImpl;
import rocks.cta.dflt.impl.core.TraceImpl;
import rocks.cta.dflt.impl.tranformer.DefaultCTATransformer;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * Class responsible for translating Invocation sequences into MDM instances.
 * 
 * @author Jonas Kunz
 *
 */
public class InvocationSequencesTranslator {

	/**
	 * 
	 * Translates the given set of Invocation Sequences into a single MDM instance.
	 * 
	 * @param methods A Mapping mapping the inpsectIT method ID to the method details
	 * @param agent information about the agent form ehich the data was imported
	 * @param invocationSequences the invocation sequences to translate
	 * @return the MDM instance
	 */
	public MonitoringDataSet translate(Map<Long, MethodIdent> methods,PlatformIdent agent, Iterable<InvocationSequenceData> invocationSequences) {
		DefaultCTATransformer transformer = new DefaultCTATransformer();
		MonitoringDataSetImpl mdm = new MonitoringDataSetImpl();

		for (InvocationSequenceData seq : invocationSequences) {
			Trace inspectITtrace = new IITTraceImpl(seq,agent, methods);
			TraceImpl trace = transformer.transform(inspectITtrace);

			EventSubTraceImpl rootSubTrace = EventSubTraceImpl.createFromSubTrace((SubTraceImpl) trace.getRoot());
			trace.setRoot(rootSubTrace);

			AbstractEvent rootEvent = null;

			LocationImpl location = (LocationImpl) rootSubTrace.getLocation();

			// convert http specific data
			if (seq.getTimerData() != null && seq.getTimerData() instanceof HttpTimerData) {
				HttpTimerData dat = (HttpTimerData) seq.getTimerData();

				HTTPRequestReceivedEventImpl event = new HTTPRequestReceivedEventImpl();
				event.setEncoding("<no-encoding>");
				event.setHTTPHeaders(Optional.ofNullable(dat.getHeaders()).map(HashMap<String, String>::new).orElse(new HashMap<>()));
				event.setHTTPParameters(Optional.ofNullable(dat.getParameters()).map(HashMap<String, String[]>::new).orElse(new HashMap<>()));
				event.setProtocol("HTTP/1.1");
				event.setRequestMethod(HTTPMethod.valueOf(dat.getHttpInfo().getRequestMethod().toUpperCase()));
				
				if(dat.getSessionId() != null) {
					//session id directly available
					event.setSessionID(dat.getSessionId());					
				} else {
					//try to extract it from the JSESSIONIDSITE cookie
					String sessionID = extractSessionIdFromCookies(dat);
					event.setSessionID(sessionID);		
					
				}
				
				Timestamp timeStamp = dat.getTimeStamp();
				event.setTimestamp(timeStamp.getTime() * 1000 * 1000 + timeStamp.getNanos());
				event.setURI(dat.getHttpInfo().getUri());
				rootEvent = event;

				String host = event.getHTTPHeaders().get().get("host");
				if (host != null) {
					location.setHost(host);
				}

			} else {
				rootEvent = new UnmonitoredEventImpl();
			}
			rootEvent.addTriggeredSubTrace(rootSubTrace);
			rootEvent.setLocation(location);

			mdm.addRootEvent(rootEvent);
		}
		return mdm;
	}

	private String extractSessionIdFromCookies(HttpTimerData dat) {
		String sessionID = null;
		String cookies = dat.getHeaders().get("cookie");
		if(cookies != null) {
			int begin = cookies.indexOf("JSESSIONIDSITE=");
			if(begin != -1) {
				begin += "JSESSIONIDSITE=".length();
				sessionID = "";
				while(begin < cookies.length()) {
					char c = cookies.charAt(begin);
					if(!Character.isLetterOrDigit(c)) {
						break;
					} else {
						sessionID += c;
						begin++;
					}
				}
			}
		}
		return sessionID;
	}
}
