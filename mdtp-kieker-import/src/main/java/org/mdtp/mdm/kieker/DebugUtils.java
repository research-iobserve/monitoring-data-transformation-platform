package org.mdtp.mdm.kieker;

import java.util.Optional;
import java.util.function.Function;

import mdm.api.core.Event;
import mdm.api.http.HTTPRequestReceivedEvent;
import rocks.cta.api.core.SubTrace;
import rocks.cta.api.core.Trace;
import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.core.callables.HTTPRequestProcessing;
import rocks.cta.api.core.callables.MethodInvocation;
import rocks.cta.api.core.callables.NestingCallable;

/**
 * 
 * Utiltiy class for printing out MDM data.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class DebugUtils {
	
	private int depth = 0;
	private static final String PREF = "| ";
	private static final String LAST_PREF = "|-";

	public void printEvent(Event event) {
		if(event instanceof HTTPRequestReceivedEvent) {
			HTTPRequestReceivedEvent httpEvent = (HTTPRequestReceivedEvent) event;
			print("HTTPEvent: session:");
			print(httpEvent.getSessionID(),Function.identity(),"<unknown-session>");
			println("");
			
		} else {
			println("Event: ("+event.getClass().getSimpleName()+")");
		}
		increaseDepth();
		event.getTriggeredSubTraces().forEach((st) -> printSubTrace(st));		
		decreaseDepth();
	}
	
	public void printTrace(Trace trace) {
		println(trace.getIdentifier(), (i) -> "Trace ("+i+")", "Trace (unindentified)");
		increaseDepth();
		printSubTrace(trace.getRoot());		
		decreaseDepth();
	}
	
	
	private void printSubTrace(SubTrace st) {
		println(st.getIdentifier(), (i) -> "Sub-Trace ("+i+")", "Sub-Trace (unindentified)");
		increaseDepth();
		printCallable(st.getRoot());	
		decreaseDepth();	
	}

	private void printCallable(Callable ca) {
		if(ca instanceof MethodInvocation) {
			printMethodInvocationInfo((MethodInvocation)ca);
			printlnNP("");
		} else if(ca instanceof HTTPRequestProcessing) {
			printHTTPRequestProcessingInfo((HTTPRequestProcessing)ca);
			printlnNP("");
		} else{
			println(Optional.empty(),(s)->"","Unknown Callable ("+ca.getClass().getSimpleName()+")");
		}
		if(ca instanceof NestingCallable) {
			increaseDepth();
			((NestingCallable)ca).getCallees().forEach(this::printCallable);
			decreaseDepth();
		}
	}


	private void printHTTPRequestProcessingInfo(HTTPRequestProcessing ca) {
		print("HTTP-REQ-PROCESS: ");
		printNP("URI:"+ca.getUri());
		
	}

	private void printMethodInvocationInfo(MethodInvocation ca) {
		print("MI: ");
		printNP(ca.getClassName(),Function.identity(),"(unknown)");
		printNP(".");
		printNP(ca.getMethodName(),Function.identity(),"(unknown)");

		printNP(", ");
		printNP(ca.getPackageName(),(p) -> "P: "+p+" ", "<nopackage>");
		printNP(", Params: ");
		printNP(ca.getParameterTypes(),(p) -> p.stream().reduce("",(s,s2)->s+","+s2), "<noargs>");
		//printNP("Time " + (ca.getTimestamp()/1000/1000)+"ms (RT: "+(ca.getResponseTime()/1000/1000)+"ms)");
	}



	private final void printNP(String str) {
		System.out.print(str);
	}
	
	private final void printlnNP(String str) {
		System.out.println(str);
	}
	
	private final void print(String str) {
		System.out.print(getCurrentPrefix()+str);
	}
	
	private final void println(String str) {
		System.out.println(getCurrentPrefix()+str);
	}

	private final <E> void printlnNP(Optional<E> value, Function<E,String> ifPresentMap, String defaultValue) {
		System.out.println(value.map(ifPresentMap).orElse(defaultValue));
	}
	
	private final <E> void printNP(Optional<E> value, Function<E,String> ifPresentMap, String defaultValue) {
		System.out.print(value.map(ifPresentMap).orElse(defaultValue));
	}


	private final <E> void println(Optional<E> value, Function<E,String> ifPresentMap, String defaultValue) {
		System.out.print(getCurrentPrefix());
		printlnNP(value,ifPresentMap,defaultValue);
	}
	
	private final <E> void print(Optional<E> value, Function<E,String> ifPresentMap, String defaultValue) {
		System.out.print(getCurrentPrefix());
		printNP(value,ifPresentMap,defaultValue);
	}
	
	private void increaseDepth() {
		depth++;
	}
	
	private void decreaseDepth() {
		depth--;
		if(depth < 0) {
			depth = 0;
		}
	}
	
	private String getCurrentPrefix() {
		String pref = "";
		for(int i=0; i<depth-1; i++){
			pref+=PREF;
		}
		if(depth > 0) {
			pref += LAST_PREF;
		}
		return pref;
	}
	
}
