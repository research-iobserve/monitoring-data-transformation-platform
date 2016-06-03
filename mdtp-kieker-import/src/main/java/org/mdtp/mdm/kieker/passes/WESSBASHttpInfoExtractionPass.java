package org.mdtp.mdm.kieker.passes;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import org.diagnoseit.spike.kieker.trace.source.CTAFilter;
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
 * This pass is responsible for extracting the HTTP information from the WESSBAS kieker
 * extensions and storing them into the corresponding generic HTTPRequestRecievedEvents of the MDM.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class WESSBASHttpInfoExtractionPass extends
		AbstractMDMUpdatePass {

	public WESSBASHttpInfoExtractionPass(Collection<String> inputFolders) {
		super(inputFolders);
	}

	/**
	 * The filter used to perform the extraction of the information.
	 */
	private WESSBASHttpInfoExtractionFilter infoExtractionFilter;

	@Override
	protected void init() {
		AnalysisController ac = getAnalysisController();

		infoExtractionFilter = new WESSBASHttpInfoExtractionFilter(new Configuration(), ac);

		this.connectPortToRecordStream(infoExtractionFilter, WESSBASHttpInfoExtractionFilter.INPUT_PORT_NAME_EVENTS);

	}

	@Override
	public void runAndWait(MonitoringDataSetImpl outputMdm) {
		infoExtractionFilter.setMonitoringDataSet(outputMdm);
		runAndWait();
	}

}
