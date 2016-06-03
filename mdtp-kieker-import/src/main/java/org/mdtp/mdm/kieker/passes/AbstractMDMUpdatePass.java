package org.mdtp.mdm.kieker.passes;

import java.util.Collection;

import mdm.dflt.impl.core.MonitoringDataSetImpl;
import kieker.analysis.AnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.AbstractPlugin;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;

/**
 * Abstract base class for an analysis pass updating an MDM instance.
 * A pass encapsualtes an entire Kieker analysis pass.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public abstract class AbstractMDMUpdatePass {

	/**
	 * The used kieker analysis controller
	 */
	private AnalysisController analysisController;
	/**
	 * The record reader used to read the records.
	 */
	private FSReader recordsReader;
	
	/**
	 * Constructor.
	 * @param inputFolders a collection of paths to the folders containing the measurement data
	 */
	public AbstractMDMUpdatePass(Collection<String> inputFolders) {
		analysisController = new AnalysisController();

		Configuration fsReaderConfig = new Configuration();
		fsReaderConfig.setStringArrayProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS, inputFolders.toArray(new String[0]));
		recordsReader = new FSReader(fsReaderConfig, analysisController);
		init();
	}
	
	/**
	 * Called after the construction has finished.
	 */
	protected abstract void init();
	
	/**
	 * Connects the given pipeline stage to the records reader.
	 * @param dst the stage to connect
	 * @param inputPortName the name of the prot to connect
	 */
	protected void connectPortToRecordStream(AbstractPlugin dst, String inputPortName) {
		try {
			analysisController.connect(recordsReader, FSReader.OUTPUT_PORT_NAME_RECORDS, dst, inputPortName);
		} catch (IllegalStateException | AnalysisConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the analysis controller used to configure the pipeline
	 */
	protected AnalysisController getAnalysisController() {
		return analysisController;
	}
	
	/**
	 * Executes the analysis.
	 */
	protected void runAndWait() {
		try {
			analysisController.run();
		} catch (IllegalStateException | AnalysisConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Configures the pipeline to update the passed MDM instance, then starts the analysis.
	 * 
	 * @param outputMdm the mdm to update
	 */
	public abstract void runAndWait(MonitoringDataSetImpl outputMdm);

}
