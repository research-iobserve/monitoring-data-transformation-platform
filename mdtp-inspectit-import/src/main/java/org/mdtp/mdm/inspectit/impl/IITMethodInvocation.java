package org.mdtp.mdm.inspectit.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import rocks.cta.api.core.Trace;
import rocks.cta.api.core.callables.MethodInvocation;
import rocks.cta.api.utils.StringUtils;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.TimerData;

public class IITMethodInvocation extends IITAbstractNestingCallable implements MethodInvocation {
	private static final String CONSTRUCTOR_SUFFIX = "<init>";
	private String signature;

	public IITMethodInvocation(InvocationSequenceData isData, IITSubTraceImpl containingTrace,
			IITAbstractNestingCallable parent) {
		super(isData, containingTrace, parent);
	}

	@Override
	public Optional<Long> getCPUTime() {
		if (isData.getTimerData() == null) {
			return Optional.empty();
		} else {
			return Optional
					.ofNullable(Math.round(isData.getTimerData().getCpuDuration() * Trace.MILLIS_TO_NANOS_FACTOR));
		}
	}

	@Override
	public Optional<String> getClassName() {
		return Optional.ofNullable(getMethodIdentifier().getClassName());
	}

	@Override
	public Optional<Long> getExclusiveCPUTime() {
		if (getCPUTime().isPresent()) {
			long exclCPUTime = getCPUTime().get();
			for (InvocationSequenceData isd : isData.getNestedSequences()) {
				TimerData timeData = isd.getTimerData();
				if (timeData != null) {
					exclCPUTime -= Math.round(timeData.getCpuDuration() * Trace.MILLIS_TO_NANOS_FACTOR);
				}
			}
			return Optional.ofNullable(exclCPUTime);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getMethodName() {
		return Optional.ofNullable(getMethodIdentifier().getMethodName());
	}

	@Override
	public Optional<String> getPackageName() {
		return Optional.ofNullable(getMethodIdentifier().getPackageName());
	}

	@Override
	public Optional<List<String>> getParameterTypes() {
		return Optional.ofNullable(Collections.unmodifiableList(getMethodIdentifier().getParameters()));
	}

	@Override
	public Optional<Map<Integer, String>> getParameterValues() {
		if (!hasParameterValues()) {
			return Optional.empty();
		}
		Map<Integer, String> result = new HashMap<Integer, String>(isData.getParameterContentData().size());
		for (ParameterContentData pcData : isData.getParameterContentData()) {
			result.put(pcData.getSignaturePosition(), pcData.getContent());
		}
		return Optional.ofNullable(Collections.unmodifiableMap(result));
	}
	
	private boolean hasParameterValues() {
		Set<ParameterContentData> pcData = isData.getParameterContentData();
		return pcData != null && !pcData.isEmpty();
	}

	@Override
	public Optional<String> getReturnType() {
		return Optional.ofNullable(getMethodIdentifier().getReturnType());
	}

	@Override
	public String getSignature() {
		if (signature == null) {
			MethodIdent mi = getMethodIdentifier();

			String fqn = mi.getPackageName() + "." + mi.getClassName() + "." + mi.getMethodName() + "(";
			boolean first = true;
			for (String par : mi.getParameters()) {
				if (first) {
					fqn += par;
					first = false;
				} else {
					fqn += "," + par;
				}

			}
			fqn += ")";
			signature = fqn;
		}

		return signature;
	}

	@Override
	public Optional<Boolean> isConstructor() {
		return getMethodName().map(v -> v.contains(CONSTRUCTOR_SUFFIX));
	}

	private MethodIdent getMethodIdentifier() {

		long mIdent = isData.getMethodIdent();
		return ((IITTraceImpl)getContainingSubTrace().getContainingTrace()).getMethodIdent(mIdent);
	}

	@Override
	public String toString() {
		return StringUtils.getStringRepresentation(this);
	}
}
