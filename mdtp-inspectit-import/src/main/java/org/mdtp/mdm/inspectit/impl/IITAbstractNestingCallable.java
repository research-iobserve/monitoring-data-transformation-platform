package org.mdtp.mdm.inspectit.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rocks.cta.api.core.TreeIterator;
import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.core.callables.NestingCallable;
import rocks.cta.api.utils.CallableIterator;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

public class IITAbstractNestingCallable extends IITAbstractTimedCallable implements NestingCallable {
	private List<Callable> children = null;

	public IITAbstractNestingCallable(InvocationSequenceData isData, IITSubTraceImpl containingTrace, IITAbstractNestingCallable parent) {
		super(isData, containingTrace, parent);
	}

	@Override
	public TreeIterator<Callable> iterator() {
		return new CallableIterator(this);
	}

	@Override
	public List<Callable> getCallees() {
		if (children == null) {
			if (isData.getNestedSequences().isEmpty()) {
				children = Collections.emptyList();
			} else {
				children = new ArrayList<>(isData.getNestedSequences().size());
				for (InvocationSequenceData isd : isData.getNestedSequences()) {
					IITAbstractCallable child = IITTraceImpl.createCallable(isd, containingTrace, this);
					children.add(child);
				}
			}

		}

		return Collections.unmodifiableList(children);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Callable> List<T> getCallees(Class<T> type) {

		List<T> result = null;
		for (Callable callable : getCallees()) {
			if (type.isAssignableFrom(callable.getClass())) {
				if (result == null) {
					result = new ArrayList();
				}
				result.add((T) callable);
			}
		}

		return result == null ? Collections.unmodifiableList(Collections.<T>emptyList()) : Collections.unmodifiableList(result);

	}

	@Override
	public int getChildCount() {
		return (int) isData.getChildCount();
	}

}
