package org.mdtp.mdm.kieker.passes;

import java.util.Collection;

import org.mdtp.mdm.kieker.filters.MDMTraceCreationFilter;

import kieker.analysis.AnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.executionRecordTransformation.ExecutionRecordTransformationFilter;
import kieker.tools.traceAnalysis.filter.traceReconstruction.TraceReconstructionFilter;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import mdm.dflt.impl.core.MonitoringDataSetImpl;

/**
 * Pass for reconstructing MDM traces from operationExecutionRecords of Kieker.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class ExecutionBasedTraceReconstructionPass extends
		AbstractMDMUpdatePass {

	public ExecutionBasedTraceReconstructionPass(Collection<String> inputFolders) {
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
			final ExecutionRecordTransformationFilter executionRecordTransformationFilter = new ExecutionRecordTransformationFilter(
					new Configuration(), ac);
			ac.connect(
					executionRecordTransformationFilter,
					AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
					systemModelRepository);

			this.connectPortToRecordStream(executionRecordTransformationFilter,
					ExecutionRecordTransformationFilter.INPUT_PORT_NAME_RECORDS);

			// Initialize, register and connect the trace reconstruction filter
			final TraceReconstructionFilter traceReconstructionFilter = new TraceReconstructionFilter(
					new Configuration(), ac);
			ac.connect(
					traceReconstructionFilter,
					AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
					systemModelRepository);
			ac.connect(
					executionRecordTransformationFilter,
					ExecutionRecordTransformationFilter.OUTPUT_PORT_NAME_EXECUTIONS,
					traceReconstructionFilter,
					TraceReconstructionFilter.INPUT_PORT_NAME_EXECUTIONS);

			// use CTAFilter to reconstruct trace into CTA
			mdmFilter = new MDMTraceCreationFilter(new Configuration(), ac);

			ac.connect(traceReconstructionFilter,
					TraceReconstructionFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
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
