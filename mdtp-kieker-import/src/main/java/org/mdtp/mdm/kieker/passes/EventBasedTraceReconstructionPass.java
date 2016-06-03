package org.mdtp.mdm.kieker.passes;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import org.diagnoseit.spike.kieker.trace.source.CTAFilter;
import org.mdtp.mdm.kieker.filters.MDMTraceCreationFilter;

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
 * Pass for reconstructing MDM traces from Before- and AfterOperationEvents of Kieker.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class EventBasedTraceReconstructionPass extends
		AbstractMDMUpdatePass {

	public EventBasedTraceReconstructionPass(Collection<String> inputFolders) {
		super(inputFolders);
	}

	/**
	 * This fitler performs the actual conversion fro mmessage traces to mdm traces.
	 */
	private MDMTraceCreationFilter mdmFilter;

	@Override
	protected void init() {
		AnalysisController ac = getAnalysisController();

		final SystemModelRepository systemModelRepository = new SystemModelRepository(
				new Configuration(), ac);

		try {
			final Configuration confTraceIdFilter = new Configuration();
			final TraceIdFilter traceIdFilter = new TraceIdFilter(confTraceIdFilter, ac);

			final Configuration confEventTraceReconstructionFilter = new Configuration();
			final EventRecordTraceReconstructionFilter eventTraceReconstructionFilter =
					new EventRecordTraceReconstructionFilter(confEventTraceReconstructionFilter, ac);

			final Configuration confTraceEvents2ExecutionAndMessageTraceFilter = new Configuration();
			final TraceEventRecords2ExecutionAndMessageTraceFilter traceEvents2ExecutionAndMessageTraceFilter =
					new TraceEventRecords2ExecutionAndMessageTraceFilter(confTraceEvents2ExecutionAndMessageTraceFilter, ac);


			this.connectPortToRecordStream(traceIdFilter, TraceIdFilter.INPUT_PORT_NAME_COMBINED);
	
	
			ac.connect(traceIdFilter, TraceIdFilter.OUTPUT_PORT_NAME_MATCH, eventTraceReconstructionFilter,EventRecordTraceReconstructionFilter.INPUT_PORT_NAME_TRACE_RECORDS);

			ac.connect(traceEvents2ExecutionAndMessageTraceFilter, AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
			ac.connect(
					eventTraceReconstructionFilter, EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID,
					traceEvents2ExecutionAndMessageTraceFilter, TraceEventRecords2ExecutionAndMessageTraceFilter.INPUT_PORT_NAME_EVENT_TRACE);

			// use CTAFilter to reconstruct trace into CTA
			mdmFilter = new MDMTraceCreationFilter(new Configuration(), ac);

			ac.connect(traceEvents2ExecutionAndMessageTraceFilter,
					TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
					mdmFilter, MDMTraceCreationFilter.INPUT_PORT_NAME_EVENTS);

		} catch (IllegalStateException | AnalysisConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void runAndWait(MonitoringDataSetImpl outputMdm) {
		mdmFilter.setMonitoringDataSet(outputMdm);
		runAndWait();
	}

}
