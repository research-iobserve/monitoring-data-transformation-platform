package org.mdtp.iobserve;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdm.api.core.MonitoringDataSet;

import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.TransformationModule;
import org.mdtp.core.impl.FileConfigurationProperty;
import org.mdtp.core.impl.PCMRepositoryModelProperty;
import org.mdtp.core.impl.PCMUsageModelProperty;
import org.mdtp.iobserve.pipeline.PipelineConfiguration;

import teetime.framework.Analysis;
import teetime.framework.AnalysisConfiguration;

/**
 * Module for performing iObserve analysis based on the Monitoring Data Model.
 * This module uses a modified T-preprocess Transformation while the rest of
 * iObserve is used without any further modifications.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class IObserveModule implements TransformationModule{

	/**
	 * Configuration property for the input repository model.
	 */
	private PCMRepositoryModelProperty inputPcmRepo = new PCMRepositoryModelProperty("inPcmRepo", "Input PCM Repository Model.");
	

	/**
	 * Configuration property for the input usage model.
	 */
	private PCMUsageModelProperty inputPcmUsage = new PCMUsageModelProperty("inPcmUsage", "Input PCM Usage Model.");
	

	/**
	 * Configuration property for the output usage model.
	 */
	private PCMUsageModelProperty outputPcmUsage = new PCMUsageModelProperty("outPcmUsage", "Output PCM Usage Model.");
	

	/**
	 * Configuration property for the input correspondence model.
	 */
	private FileConfigurationProperty correspondenceModel = new FileConfigurationProperty("correspondences", "The Correspondence model.");
	
	private List<FileConfigurationProperty> allProperties = Arrays.asList(
			inputPcmRepo,
			inputPcmUsage,
			outputPcmUsage,
			correspondenceModel);
	
	
	@Override
	public List<? extends ConfigurationProperty<?>> getConfiguration() {
		return Collections.unmodifiableList(allProperties);
	}

	@Override
	public void validateConfiguration(ErrorBuffer errors) {
		
		if(!correspondenceModel.isPathValid() || !correspondenceModel.getFile().get().exists()) {
			errors.addError("The path \"" + correspondenceModel.getValue().orElse("") + "\" does not point to a valid "
					+ "correspondence model, as the file does not exist.");
		}

		if(!inputPcmUsage.isModelLoadable()) {
			errors.addError("The path \"" + inputPcmUsage.getValue().orElse("") + "\" does not point to a loadable PCM Usage model");
		}

		if(!inputPcmRepo.isModelLoadable()) {
			errors.addError("The path \"" + inputPcmRepo.getValue().orElse("") + "\" does not point to a loadable PCM Repository model");
		}
		

		if(!outputPcmUsage.isPathValid()) {
			errors.addError("The path given for the output usage model \"" + outputPcmUsage.getValue().orElse("") + "\" is not a correct file path for a usage model.");
		} else if ( outputPcmUsage.getFile().get().exists()) {
			errors.addWarning("The given output usage model \"" + outputPcmUsage.getValue().orElse("") + "\" already exists and will be overwritten");
		}
	}
	
	

	@Override
	public void execute(MonitoringDataSet monitoringData) {
		
		//delete the output file if it already exists
		File outputFile = outputPcmUsage.getFile().get();
		if(outputFile.exists()) {
			outputFile.delete();
		}
		
		//configure the iobserve pipeline with the configured input values
		PipelineConfiguration config = new PipelineConfiguration(
				correspondenceModel.getValue().get(),
				inputPcmRepo.getValue().get(),
				inputPcmUsage.getValue().get(),
				outputPcmUsage.getValue().get(),
				monitoringData);
		
		//execute the analysis
		final Analysis<AnalysisConfiguration> analysis = new Analysis<AnalysisConfiguration>(config);
		analysis.executeBlocking();
	}

}
