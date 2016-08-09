package org.mdtp.mdm.kieker.filters;

import java.util.HashMap;
import java.util.Map;


import mdm.api.core.MonitoringDataSet;
import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import rocks.cta.api.core.Trace;

public abstract class AbstractTraceIDLookupFilter  extends AbstractFilterPlugin {
	
	public AbstractTraceIDLookupFilter(Configuration configuration, IProjectContext projectContext) {
		super(configuration, projectContext);
	}

	/**
	 * A Map to provide O(1) lookup of traces by their ID.
	 * This map is initialised at the first time it is used for the current pass, see {@link #getTraceIdMap())
	 */
	private Map<Long,Trace> traceIDMap = null;
	

	@Override
	public boolean init() {
		traceIDMap = null;
		return super.init();
	}

	/**
	 * Computes a map mapping traceIDs to their trace instances in the mdm.
	 * @param mdm the mdm to use, must be the same on every call after init
	 * @return the mapping
	 */
	protected Map<Long, Trace> getTraceIdMap(MonitoringDataSet mdm) {
		if(traceIDMap == null) {
			synchronized(this) {
				if(traceIDMap == null) {
					traceIDMap = new HashMap<>();
					mdm.getTraces().forEach(t -> {
						Object prev = traceIDMap.put((Long)(t.getIdentifier().get()), t);
						if(prev != null) {
						}
					});
				}				
			}
		}
		return traceIDMap;
	}

}
