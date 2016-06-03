package mdm.dflt.impl.http;

import java.util.Map;
import java.util.Optional;

import rocks.cta.api.core.callables.HTTPMethod;
import mdm.api.deployment.EJBDeployedEvent;
import mdm.api.http.HTTPRequestReceivedEvent;
import mdm.dflt.impl.core.AbstractTimedEvent;


/**
 * Default implementation for {@link HTTPRequestReceivedEvent}.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class HTTPRequestReceivedEventImpl extends AbstractTimedEvent implements HTTPRequestReceivedEvent{

	/**
	 * The identifier of the user session, may be null.
	 */
	private String sessionID;
	
	/**
	 * The uri of the request, lreative to the host. The host is specified by the Location of this event.
	 */
	private String uri;
	
	/**
	 * The http method of the recieved request
	 */
	private HTTPMethod method;
	/**
	 * A map of the parameters ent with the request, including the URL encoded ones.
	 */
	private Map<String,String[]> httpParameters;

	/**
	 * The http protocol used for the request, e.g. HTTP/1.1
	 */
	private String protocol;

	/**
	 * The encoding used for the request, e.g. UTF-8
	 */
	private String encoding;
	
	
	/**
	 * A map containing the http headers sent with the request.
	 */
	private Map<String,String> httpHeaders;
	
	@Override
	public Optional<String> getSessionID() {
		return Optional.ofNullable(sessionID);
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public Optional<HTTPMethod> getRequestMethod() {
		return Optional.ofNullable(method);
	}

	@Override
	public Optional<Map<String, String[]>> getHTTPParameters() {
		return Optional.ofNullable(httpParameters);
	}

	@Override
	public Optional<Map<String, String>> getHTTPHeaders() {
		return Optional.ofNullable(httpHeaders);
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public void setRequestMethod(HTTPMethod method) {
		this.method = method;
	}

	public void setHTTPParameters(Map<String, String[]> httpParameters) {
		this.httpParameters = httpParameters;
	}

	public void setHTTPHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	@Override
	public Optional<String> getEncoding() {
		return Optional.ofNullable(encoding);
	}

	@Override
	public Optional<String> getProtocol() {
		return Optional.ofNullable(protocol);
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	

}
