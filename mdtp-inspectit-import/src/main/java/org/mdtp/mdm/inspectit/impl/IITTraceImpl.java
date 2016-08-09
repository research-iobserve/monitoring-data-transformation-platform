package org.mdtp.mdm.inspectit.impl;

import java.util.Map;

import rocks.cta.api.core.SubTrace;
import rocks.cta.api.core.Trace;
import rocks.cta.api.core.TreeIterator;
import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.utils.CallableIteratorOnTrace;
import rocks.cta.api.utils.StringUtils;
import rocks.cta.api.utils.SubTraceIterator;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

public class IITTraceImpl extends IITAbstractIdentifiableImpl implements Trace {

	protected static IITAbstractCallable createCallable(InvocationSequenceData isData, IITSubTraceImpl containingTrace, IITAbstractNestingCallable parent) {
		if (isData.getSqlStatementData() != null) {
			return new IITDatabaseInvocation(isData, containingTrace, parent);
		} else if (isData.getTimerData() != null && isData.getTimerData() instanceof HttpTimerData) {
			return new IITHTTPRequestProcessing(isData, containingTrace, parent);
		} else {
			return new IITMethodInvocation(isData, containingTrace, parent);
		}
	}

	private SubTrace root;
	private Map<Long, MethodIdent> methodIdents;
	PlatformIdent agent;

	public IITTraceImpl(InvocationSequenceData root,PlatformIdent agent,  Map<Long,MethodIdent> methods) {
		super(root.getId());
		this.root = new IITSubTraceImpl(this, root);
		this.methodIdents = methods;
		this.agent = agent;
	}

	@Override
	public TreeIterator<Callable> iterator() {
		return new CallableIteratorOnTrace(root);
	}

	@Override
	public SubTrace getRoot() {
		return root;
	}

	@Override
	public long getTraceId() {
		return root.getSubTraceId();
	}

	@Override
	public String toString() {
		return StringUtils.getStringRepresentation(this);
	}

	@Override
	public int size() {
		return root.size();
	}

	@Override
	public TreeIterator<SubTrace> subTraceIterator() {
		return new SubTraceIterator(root);
	}

	
	MethodIdent getMethodIdent(long id) {
		return methodIdents.get(id);
	}


	@Override
	public long getExclusiveTime() {
		return getResponseTime();
	}

	@Override
	public long getResponseTime() {
		if (root == null) {
			return 0;
		} else {
			return root.getResponseTime();
		}
	}

	public PlatformIdent getPlatform() {
		return agent;
	}

}
