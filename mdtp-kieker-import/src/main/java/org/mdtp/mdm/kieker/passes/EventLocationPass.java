package org.mdtp.mdm.kieker.passes;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import org.diagnoseit.spike.kieker.trace.source.CTAFilter;
import org.mdtp.mdm.kieker.filters.EventLocationFilter;
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
 * This pass is responsible for extracting the location of events from TraceMetadata records.
 * This pass is necessary, as MessageTraces do not provide this information.
 * The location will only be written to the vent if it has none assigned yet.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class EventLocationPass extends
		AbstractMDMUpdatePass {

	public EventLocationPass(Collection<String> inputFolders) {
		super(inputFolders);
	}

	/**
	 * The filter used to perform the extraction of the information.
	 */
	private EventLocationFilter infoExtractionFilter;

	@Override
	protected void init() {
		AnalysisController ac = getAnalysisController();

		infoExtractionFilter = new EventLocationFilter(new Configuration(), ac);

		this.connectPortToRecordStream(infoExtractionFilter, EventLocationFilter.INPUT_PORT_NAME_EVENTS);

	}

	@Override
	public void runAndWait(MonitoringDataSetImpl outputMdm) {
		infoExtractionFilter.setMonitoringDataSet(outputMdm);
		runAndWait();
	}

}
