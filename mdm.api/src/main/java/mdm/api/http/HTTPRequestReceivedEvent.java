package mdm.api.http;

import java.util.Map;
import java.util.Optional;

import rocks.cta.api.core.callables.HTTPMethod;
import mdm.api.core.TimedEvent;

/**
 * Event type representing an incoming HTTP request.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface HTTPRequestReceivedEvent extends SessionAwareEvent, TimedEvent {
	
	/**
	 * @return the URI relative to the host to which the request referred
	 */
	String getURI();
	/**
	 * @return the HTTP method of the request
	 */
	Optional<HTTPMethod> getRequestMethod();
	/**
	 * @return a Map contiang all paramteres which were sent with this request, including URL encoded parameters.
	 */
	Optional<Map<String,String[]>> getHTTPParameters();
	/**
	 * @return a Map mapping the name of the present headers to their value.
	 */
	Optional<Map<String,String>> getHTTPHeaders();

	/**
	 * @return the HTTP method of the request
	 */
	Optional<String> getEncoding();
	
	/**
	 * @return the HTTP protocol, e.g. HTTP/1.1
	 */
	Optional<String> getProtocol();

}
