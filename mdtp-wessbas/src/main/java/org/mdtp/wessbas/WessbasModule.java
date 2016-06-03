package org.mdtp.wessbas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import mdm.api.core.MonitoringDataSet;
import mdm.dflt.impl.serialization.KryoMDMDeserializer;
import mdm.dflt.impl.serialization.MDMDeserializer;
import net.sf.markov4jmeter.behaviormodelextractor.BehaviorModelExtractor;
import net.sf.markov4jmeter.behaviormodelextractor.CommandLineArgumentsHandler;
import net.sf.markov4jmeter.behaviormodelextractor.extraction.ExtractionException;
import net.sf.markov4jmeter.behaviormodelextractor.extraction.transformation.RBMToRBMUnifier;

import org.apache.commons.io.FileUtils;
import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.TransformationModule;
import org.mdtp.core.impl.FileConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wessbas.commons.parser.ParseException;

public class WessbasModule implements TransformationModule {

	private static final Logger LOG = LoggerFactory.getLogger(WessbasModule.class);
	
	
	private FileConfigurationProperty tempDirectory = new FileConfigurationProperty("temporary folder", "Temporary Folder to use for storing temp files");

	@Override
	public List<? extends ConfigurationProperty<?>> getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validateConfiguration(ErrorBuffer errors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(MonitoringDataSet monitoringData) {

		try {
			File tempDir = tempDirectory.getFile().get();
			String prefix = tempDir.getAbsolutePath() + File.separator;
			LOG.info("Using temp dir " + prefix);
			
			if (tempDir.exists()) {
				FileUtils.deleteDirectory(tempDir);
			}
			FileUtils.forceMkdir(tempDir);

			LOG.info("Extracting sessions.dat file....");
			//Step 1: generate a sessions.dat file from the MDM
			String sessionsFile = prefix+"sessions.dat";
			FileOutputStream fout = FileUtils.openOutputStream(new File(sessionsFile));
			MDMToSessionsDatConverter converter = new MDMToSessionsDatConverter();	
			converter.convert(monitoringData.getRootEvents().stream(), fout);
			fout.close();


			LOG.info("Perfomring behavior clustering...");
			//step 2: execute behaviour clustering
			File behaviourDir = new File(prefix+"behaviour");
			FileUtils.forceMkdir(behaviourDir);
			
			String[] cmdArgs = {
				"-i",""+sessionsFile+"",
				"-o",""+behaviourDir.getAbsolutePath()+"",
				"-c","xmeans",
				"-min","1",
				"-max","4",
				
			};
			
			BehaviorModelExtractor.main(cmdArgs);

			
			
			
			
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 

	}
	
	public static void main(String[] args) {
		WessbasModule mod = new WessbasModule();
		mod.tempDirectory.setValue("temp");
		
		MonitoringDataSet mdm = readMdmFile("CoCoMe.mdm");
		
		mod.execute(mdm);
		
	}
	
	private static MonitoringDataSet readMdmFile(String path) {
		File file = new File(path);
		MDMDeserializer deserializer = new KryoMDMDeserializer();
		
		try {
			
			deserializer.setSource(new FileInputStream(file));			
			MonitoringDataSet mdm;
			mdm = (MonitoringDataSet) deserializer.readNext();
			deserializer.close();
			
			return mdm;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
