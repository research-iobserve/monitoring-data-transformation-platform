package org.mdtp.mdm.inspectit.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import rocks.cta.api.core.AdditionalInformation;
import rocks.cta.api.core.SubTrace;
import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.core.callables.NestingCallable;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * Copied from the diagnoseIT project with minor updates to match the current inspectIT version.
 *
 */
public class IITAbstractCallable extends IITAbstractIdentifiableImpl implements Callable {
	protected final InvocationSequenceData isData;
	protected IITAbstractNestingCallable parent = null;
	protected final IITSubTraceImpl containingTrace;
	
	public IITAbstractCallable(InvocationSequenceData isData, IITSubTraceImpl containingTrace, IITAbstractNestingCallable parent) {
		super(isData.getId());
		this.isData = isData;
		this.containingTrace = containingTrace;
		this.parent = parent;
	}

	@Override
	public Optional<Collection<AdditionalInformation>> getAdditionalInformation() {
		return Optional.empty();
	}

	@Override
	public <T extends AdditionalInformation> Optional<Collection<T>> getAdditionalInformation(Class<T> arg0) {
		return Optional.empty();
	}

	@Override
	public SubTrace getContainingSubTrace() {
		return containingTrace;
	}

	@Override
	public Optional<List<String>> getLabels() {
		return Optional.empty();
	}

	@Override
	public NestingCallable getParent() {
		return parent;
	}

	@Override
	public long getTimestamp() {
		return isData.getTimeStamp().getTime();
	}

}
