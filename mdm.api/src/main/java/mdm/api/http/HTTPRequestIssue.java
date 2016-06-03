package mdm.api.http;

import mdm.api.core.EventTrigger;

/**
 * Callable type representing the issuing of an HTTP request.
 * If the request stays within the system (e.g. a call to an internal service) this call triggers a 
 * HTTPRequestRecievedEvent for the request.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 * @param <E> the concrete HTTPRequestRecieved type
 */
public interface  HTTPRequestIssue<E extends HTTPRequestReceivedEvent> extends EventTrigger<E>{

}
