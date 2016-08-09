package org.mdtp.iobserve;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mdm.api.core.MonitoringDataSet;

import org.iobserve.analysis.AnalysisMain;
import org.iobserve.analysis.AnalysisMainParameterBean;
import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.TransformationModule;
import org.mdtp.core.impl.FileConfigurationProperty;
import org.mdtp.core.impl.PCMRepositoryModelProperty;
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
	 * Configuration property for the input model.
	 */
	private FileConfigurationProperty modelsDir = new PCMRepositoryModelProperty("modelsDir", "Directory with all required Models");
		

	/**
	 * Configuration property for the logging output of iObserve.
	 */
	private FileConfigurationProperty loggingDir = new FileConfigurationProperty("loggingDir", "The directory to write the logs to.");
	
	private List<FileConfigurationProperty> allProperties = Arrays.asList(
			modelsDir,
			loggingDir);
	
	
	@Override
	public List<? extends ConfigurationProperty<?>> getConfiguration() {
		return Collections.unmodifiableList(allProperties);
	}

	@Override
	public void validateConfiguration(ErrorBuffer errors) {
		
		if(!modelsDir.isPathValid() || !modelsDir.getFile().get().exists()) {
			errors.addError("The path \"" + modelsDir.getValue().orElse("") + "\" does not point to a valid directory");
		}
		//TODO: maybe add mechanisms to check if all required models are there?
		if(!loggingDir.isPathValid() || !loggingDir.getFile().get().exists()) {
			errors.addError("The path \"" + loggingDir.getValue().orElse("") + "\" does not point to a valid directory");
		}
	}
	
	

	@Override
	public void execute(MonitoringDataSet monitoringData) {
		
		AnalysisMainParameterBean iObserveConfig = new AnalysisMainParameterBean();
		iObserveConfig.setDirLogging(loggingDir.getValue().get());
		iObserveConfig.setDirPcmModels(this.modelsDir.getValue().get());
		//ugly workaround required as the method is not public yet...
		try {
			Method init = AnalysisMain.class.getDeclaredMethod("init", AnalysisMainParameterBean.class);
			init.setAccessible(true);
			init.invoke(AnalysisMain.getInstance(), iObserveConfig);
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		PipelineConfiguration config = new PipelineConfiguration(monitoringData);
		
		//execute the analysis
		final Analysis<AnalysisConfiguration> analysis = new Analysis<AnalysisConfiguration>(config);
		analysis.executeBlocking();
	}

}
