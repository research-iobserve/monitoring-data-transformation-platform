package org.mdtp.mdm.inspectit.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import rocks.cta.api.core.callables.HTTPMethod;
import rocks.cta.api.core.callables.HTTPRequestProcessing;
import rocks.cta.api.utils.StringUtils;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

public class IITHTTPRequestProcessing extends IITAbstractNestingCallable implements HTTPRequestProcessing {
	private HttpTimerData httpData;

	public IITHTTPRequestProcessing(InvocationSequenceData isData, IITSubTraceImpl containingTrace, IITAbstractNestingCallable parent) {
		super(isData, containingTrace, parent);
		httpData = (HttpTimerData) isData.getTimerData();
	}

	@Override
	public Optional<Map<String, String>> getHTTPAttributes() {
		return Optional.ofNullable(httpData.getAttributes()).map(Collections::unmodifiableMap);
	}

	@Override
	public Optional<Map<String, String>> getHTTPHeaders() {
		return Optional.ofNullable(httpData.getHeaders()).map(Collections::unmodifiableMap);
	}

	@Override
	public Optional<Map<String, String[]>> getHTTPParameters() {
		return Optional.ofNullable(httpData.getParameters()).map(Collections::unmodifiableMap);
	}

	@Override
	public Optional<Map<String, String>> getHTTPSessionAttributes() {
		return Optional.ofNullable(httpData.getSessionAttributes()).map(Collections::unmodifiableMap);
	}

	@Override
	public Optional<HTTPMethod> getRequestMethod() {
		return Optional.ofNullable(HTTPMethod.valueOf(httpData.getHttpInfo().getRequestMethod().toUpperCase()));
	}

	@Override
	public String getUri() {
		return httpData.getHttpInfo().getUri();
	}

	@Override
	public String toString() {
		return StringUtils.getStringRepresentation(this);
	}
}
