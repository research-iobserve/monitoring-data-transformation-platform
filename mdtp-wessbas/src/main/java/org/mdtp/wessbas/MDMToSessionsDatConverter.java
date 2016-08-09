package org.mdtp.wessbas;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mdm.api.core.Event;
import mdm.api.core.EventSubTrace;
import mdm.api.http.HTTPRequestReceivedEvent;
import mdm.api.http.SessionAwareEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.core.callables.HTTPMethod;
import rocks.cta.api.core.callables.MethodInvocation;
import rocks.cta.api.core.callables.NestingCallable;

/**
 * 
 * Class for transfomring MDM events into a WESSBAS compatible session log.
 * @author Jonas Kunz
 *
 */
public class MDMToSessionsDatConverter {
	
	private static final Logger LOG = LoggerFactory.getLogger(MDMToSessionsDatConverter.class);
	
	/**
	 * #Converts the given set of events into a session log.
	 * @param events the events to convert
	 * @param out the output stream to write the session log to.
	 */
	public void convert(Stream<Event> events, OutputStream out) {
		
		Comparator<SessionAwareEvent> timingComparator 
		= (e1,e2) -> Long.compare(getRootInvocation(e1).getTimestamp(), getRootInvocation(e2).getTimestamp());
		
		LOG.info("Grouping Events by session...");
		
		//fitler out session aware events, group them by their session and order by time
		Map<String,List<SessionAwareEvent>> sessionsMap =
		events
		.filter((e) -> e instanceof SessionAwareEvent)
		.map((e) -> (SessionAwareEvent) e)
		.filter((e) -> e.getSessionID().isPresent())
		.filter(e -> getRootInvocation(e) != null)
		.sorted(timingComparator)
		.collect(Collectors.groupingBy((e) -> e.getSessionID().get()));
		
		LOG.info("Grouping done.");

		LOG.info("Writing sessions file...");
		writeFile(sessionsMap,out);
		LOG.info("Writing done.");
	}


	private void writeFile(Map<String, List<SessionAwareEvent>> sessionsMap, OutputStream out) {
		
		PrintStream ps = new PrintStream(out);
		
		boolean first = true;
		for(Entry<String, List<SessionAwareEvent>> session : sessionsMap.entrySet()) {
			String sessionId = session.getKey();
			StringBuffer entry = new StringBuffer();
			
			entry.append(sessionId);
			for(SessionAwareEvent event : session.getValue()) {
				
				//guaranteed to not be null
				MethodInvocation rootInvocation = getRootInvocation(event);
				
				entry
				.append(";\"")
				.append(rootInvocation.getMethodName().get()).append("\":")
				.append(rootInvocation.getTimestamp()).append(":")
				.append(rootInvocation.getTimestamp()+rootInvocation.getResponseTime());		
				
				//if available, write out the HTTP information
				if (event instanceof HTTPRequestReceivedEvent) {
					appendHTTPInfo(entry,(HTTPRequestReceivedEvent)event);
				}
			}
			
			if(first){
				first = false;
			} else {
				ps.println();
			}
			ps.print(entry.toString());
			
		}
	}


	private void appendHTTPInfo(StringBuffer entry,HTTPRequestReceivedEvent event) {
		
		try {
			
			String uri = event.getURI();
			if(uri == null) {
				return;
			}
			
			String host_port = event.getLocation().get().getHost();
			String host = host_port;
			String port = "";
			if(host_port.contains(":")) {
				int i = host_port.indexOf(":");
				host = host_port.substring(0,i);
				port = host_port.substring(i+1);
			}
			
			String protocol = event.getProtocol().orElse("HTTP/1.1");
			String encoding = event.getEncoding().orElse("<no-encoding>");
			String method = event.getRequestMethod().orElse(HTTPMethod.GET).toString();
			String queryString = "<no-query-string>";
			if(event.getHTTPParameters().isPresent()) {
				queryString = encodeQueryString(event.getHTTPParameters().get());
			}
			

			entry.append(":").append(uri);
			entry.append(":").append(port);
			entry.append(":").append(host);
			entry.append(":").append(protocol);
			entry.append(":").append(method);
			entry.append(":").append(queryString);
			entry.append(":").append(encoding);	
		}catch(NoSuchElementException e) {
			return; //do nothing, some data is missing therefore omit writing
		}
		
	}


	/**
	 * @param trace the trace to scan
	 * @return the highest level method invocation found in the execution trace tree
	 */
	private MethodInvocation getRootInvocation(Event e) {
		if(e.getTriggeredSubTraces().size() != 1) {
			return null;
		}
		EventSubTrace trace = e.getTriggeredSubTraces().get(0);
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
	
	private String encodeQueryString(Map<String,String[]> params) {
		try {
			if(params.isEmpty()) {
				return "<no-query-string>";
			}
			StringBuffer result = new StringBuffer();
			for(String key : params.keySet()) {
				String encodedKey = URLEncoder.encode(key, "UTF-8");
				for(String value : params.get(key)) {
					String encodedValue = "";
					if(value != null) {
						encodedValue = "=" + URLEncoder.encode(value, "UTF-8");
					}
					
					if(result.length() > 0) {
						result.append("&");
					}
					result.append(encodedKey+encodedValue);
					
				}
			}
			return result.toString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
