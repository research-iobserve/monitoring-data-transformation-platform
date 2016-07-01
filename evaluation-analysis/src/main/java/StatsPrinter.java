import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import mdm.api.core.Event;
import mdm.api.core.MonitoringDataSet;
import mdm.api.http.HTTPRequestReceivedEvent;
import mdm.api.http.SessionAwareEvent;

import org.mdtp.mdm.inspectit.DebugUtils;
import org.mdtp.mdm.inspectit.FSInspectITModule;
import org.mdtp.mdm.kieker.KiekerImportModule;

import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.core.callables.NestingCallable;



public class StatsPrinter {
	
	private static final List<String> activitesOrdered = Arrays.asList(
			"visit main page",
			"login",
			"logout",
			"view cart",
			"browse page",
			"Visit productpage",
			"add last product to cart",
			"search keyword",
			"checkout"
			);
	

	public static void main(String[] args) throws IOException {
		
		List<String> productUris = loadProductUris();
		List<String> browseUris = loadBrowseUris();
		
		NamedCounter1D requestCounter = new NamedCounter1D();
		NamedCounter1D stateCounter = new NamedCounter1D();
		NamedCounter2D transitionCounter = new NamedCounter2D();
		
		
		
		
		KiekerImportModule importer = new KiekerImportModule();
		importer.getInDirProperty().setValue("C:\\Users\\JKU\\Desktop\\thesis\\measurement data\\broadleaf\\measurment\\kieker-event\\script_default_behaviour\\1200");
		MonitoringDataSet mdm = importer.importMonitoringDataModel();
		
		
//		FSInspectITModule importer = new FSInspectITModule();
//		importer.getStorageFolder().setValue("C:\\Users\\JKU\\Desktop\\thesis\\measurement data\\broadleaf\\measurment\\inspectIT\\script_default_behaviour\\1200\\ae38df60-17c1-445a-ba60-840239587651");
//		MonitoringDataSet mdm = importer.importMonitoringDataModel();
		
		//split the data into session
		Map<String, List<HTTPRequestReceivedEvent>> sessions =
		mdm.getRootEvents().stream()
		.map((e) -> (HTTPRequestReceivedEvent)e)
		.filter((e) -> e.getSessionID().isPresent())
		.collect(Collectors.groupingBy((e) -> e.getSessionID().get(), Collectors.toList()));
		
		
		sessions.values().forEach((requests) -> requests.sort((a,b) -> Long.compare(a.getTimestamp(), b.getTimestamp())));
		
		long minTimeStamp = Long.MAX_VALUE;
		long maxTimeStamp = Long.MIN_VALUE;
		
		long sumMaxCallTreeDepth = 0;
		long sumCallableCount = 0;
		
		for(Object session : sessions.keySet()) {
			List<HTTPRequestReceivedEvent> requests = sessions.get(session);
			String previousAction = null;
			for(HTTPRequestReceivedEvent e : requests) {
				minTimeStamp = Math.min(minTimeStamp,e.getTimestamp());
				maxTimeStamp = Math.max(maxTimeStamp, e.getTimestamp());
				
				if(e.getURI() == null) {
					break; //reached end of monitoring
				}
				
				sumMaxCallTreeDepth += getMaxDepth(e.getTriggeredSubTraces().get(0).getRoot());
				sumCallableCount += getNumCallables(e.getTriggeredSubTraces().get(0).getRoot());
				
				String requestType = getRequestType(productUris, browseUris, e, requests);
				requestCounter.addOne(requestType);
				
				String action = getRequestAction(productUris, browseUris, e, requests);
				if(previousAction != null) {
					if(previousAction.equals(action)) {
						//the following actions may not transition to themselves but can consists out of more than one request
						if(!action.equals("login") && !action.equals("logout") && !action.equals("checkout")) {
							transitionCounter.addOne(previousAction, action);
							stateCounter.addOne(action);
						}
					} else {
						transitionCounter.addOne(previousAction, action);
						stateCounter.addOne(action);
					}
				} else {
					stateCounter.addOne(action);
				}
				previousAction = action;
			}
			transitionCounter.addOne(previousAction, "$");
		}
		requestCounter.normalize(100);
		stateCounter.normalize(100);
		transitionCounter.normalize(100);
		
		System.out.println("Measurement Duration: "+(((maxTimeStamp-minTimeStamp) / 1000 / 1000) / 1000.0)+" seconds");
		int size = mdm.getRootEvents().size();
		System.out.println(size+" events in total with");
		System.out.println((sumCallableCount*1.0/size)+" callables per event.");
		System.out.println((sumMaxCallTreeDepth*1.0/size)+" average call tree depth");
		
		System.out.println("State Distribution:");
		activitesOrdered.forEach((s) -> System.out.print(s+","));
		System.out.println();
		activitesOrdered.forEach((s) -> System.out.print(stateCounter.getCount(s)+","));
		System.out.println();

		System.out.println("State Transition Distribution Matrix:");
		activitesOrdered.forEach((s) -> System.out.print(","+s));
		System.out.println();
		for(String srcState : activitesOrdered) {
			System.out.print(srcState+",");
			activitesOrdered.forEach((s) -> System.out.print(transitionCounter.getCount(srcState,s)+","));
			System.out.println(transitionCounter.getCount(srcState,"$"));
			
		}
		
		
		/*
		
		
		for(Event e : mdm.getRootEvents()) {
			String uri = ((HTTPRequestReceivedEvent)e).getURI();
			String requestType;
			requestType = getRequestType(productUris, browseUris, uri);
			requestCounter.addOne(requestType);
		}
		requestCounter.normalize(100);
		System.out.println(requestCounter);
		*/
		
		
	}

	private static long getMaxDepth(Callable callable) {
		if(callable instanceof NestingCallable) {
			long maxDepth = 0;
			for(Callable sub : ((NestingCallable)callable).getCallees()) {
				maxDepth = Math.max(maxDepth, getMaxDepth(sub));
			}
			return maxDepth+1;			
		} else {
			return 1;
		}
	}
	

	private static long getNumCallables(Callable callable) {
		if(callable instanceof NestingCallable) {
			long sum = 0;
			for(Callable sub : ((NestingCallable)callable).getCallees()) {
				sum += getMaxDepth(sub);
			}
			return sum+1;			
		} else {
			return 1;
		}
	}

	private static String getRequestType(List<String> productUris, List<String> browseUris, HTTPRequestReceivedEvent e, List<HTTPRequestReceivedEvent> session) {
		String uri = e.getURI();
		String nextUri = "";
		int nextIndex = session.indexOf(e) + 1;
		if(nextIndex < session.size()) {
			nextUri = session.get(nextIndex).getURI();
		}
		String requestType = "";
		if(uri.equals("/")) {
			requestType = "visit main page";
		} else if(uri.equals("/login")) {
			requestType = "login";
		}  else if(uri.equals("/login_post.htm")) {
			requestType = "post login";
		}  else if(uri.equals("/logout")) {
			requestType = "logout";
		}  else if(uri.equals("/cart")) {
			if(nextUri.startsWith("/cart/remove")) {
				requestType = "view cart for logout";
			} else {
				requestType = "view cart";
			}
		} else if(uri.equals("/cart/add")) {
			requestType = "add to cart";
		}else if(productUris.contains(uri)) {
			requestType = "browse product";
		}else if(browseUris.contains(uri)) {
			requestType = "browse category page";
		} else if(uri.startsWith("/search")) {
			requestType = "search";
		} else if(uri.startsWith("/checkout"))  {
			requestType = uri.substring(1);
		} else if ( uri.startsWith("/confirmation/")) {
			requestType = "confirmation/${id}";
			
		} else if(uri.startsWith("/cart/remove")) {
			requestType = "remove from cart for logout";
		} else {
			System.out.println("unknown uri: " + uri);
			throw new RuntimeException();
		}
		return requestType;
	}
	
	private static String getRequestAction(List<String> productUris, List<String> browseUris, HTTPRequestReceivedEvent e, List<HTTPRequestReceivedEvent> session) {
		String uri = e.getURI();
		String nextUri = "";
		int nextIndex = session.indexOf(e) + 1;
		if(nextIndex < session.size()) {
			nextUri = session.get(nextIndex).getURI();
		}
		String requestType = "";
		

		if(uri.equals("/")) {
			requestType = "visit main page";
		} else if(uri.equals("/login") || uri.equals("/login_post.htm") ) {
			requestType = "login";
		}  else if(uri.equals("/logout")) {
			requestType = "logout";
		}  else if(uri.equals("/cart")) {
			if(nextUri.startsWith("/cart/remove")) {
				requestType = "logout";
			} else {
				requestType = "view cart";
			}
		} else if(uri.equals("/cart/add")) {
			requestType = "add last product to cart";
		}else if(productUris.contains(uri)) {
			requestType = "Visit productpage";
		}else if(browseUris.contains(uri)) {
			requestType = "browse page";
		} else if(uri.startsWith("/search")) {
			requestType = "search keyword";
		} else if(uri.startsWith("/checkout") || uri.startsWith("/confirmation/"))  {
			requestType = "checkout";
		} else if(uri.startsWith("/cart/remove")) {
			requestType = "logout";
		} else {
			System.out.println("unknown uri: " + uri);
			throw new RuntimeException();
		}
		return requestType;
	}

	private static List<String> loadProductUris() throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("products_default_catalog.csv"));
		List<String> result = new ArrayList<String>();
		for(String line : lines) {
			result.add("/"+line.split(",")[1].replaceAll("\"", ""));
		}
		System.out.println(result);
		return result;
	}
	

	private static List<String> loadBrowseUris() throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("browsingPages_default_catalog.csv"));
		List<String> result = new ArrayList<String>();
		for(String line : lines) {
			result.add("/"+line.substring(0,line.length()-1));
		}
		System.out.println(result);
		return result;
	}

}
