package org.mdtp.mdm.kieker.passes;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import org.diagnoseit.spike.kieker.trace.source.CTAFilter;
import org.mdtp.mdm.kieker.filters.IObserveEventsTranslationFilter;
import org.mdtp.mdm.kieker.filters.WESSBASHttpInfoExtractionFilter;

import rocks.cta.api.core.Trace;
import kieker.analysis.AnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.flow.EventRecordTraceReconstructionFilter;
import kieker.analysis.plugin.filter.select.TraceIdFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.executionRecordTransformation.ExecutionRecordTransformationFilter;
import kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter;
import kieker.tools.traceAnalysis.filter.traceReconstruction.TraceReconstructionFilter;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import mdm.dflt.impl.core.MonitoringDataSetImpl;

/**
 * 
 * This pass is responsible for translating iObserve specific events into their corresponding generic MDM events.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class IObserveEventsTranslationPass extends
		AbstractMDMUpdatePass {

	public IObserveEventsTranslationPass(Collection<String> inputFolders) {
		super(inputFolders);
	}

	/**
	 * The mdm which will be updated
	 */
	private IObserveEventsTranslationFilter translationFilter;

	@Override
	protected void init() {
		AnalysisController ac = getAnalysisController();
		translationFilter = new IObserveEventsTranslationFilter(new Configuration(), ac);

		this.connectPortToRecordStream(translationFilter, IObserveEventsTranslationFilter.INPUT_PORT_NAME_EVENTS);

	}

	@Override
	public void runAndWait(MonitoringDataSetImpl outputMdm) {
		translationFilter.setMonitoringDataSet(outputMdm);
		runAndWait();
	}

}
