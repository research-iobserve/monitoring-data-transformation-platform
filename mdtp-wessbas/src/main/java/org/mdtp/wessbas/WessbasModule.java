package org.mdtp.wessbas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import mdm.api.core.MonitoringDataSet;
import mdm.dflt.impl.serialization.KryoMDMDeserializer;
import mdm.dflt.impl.serialization.MDMDeserializer;
import net.sf.markov4jmeter.behaviormodelextractor.BehaviorModelExtractor;
import net.sf.markov4jmeter.m4jdslmodelgenerator.M4jdslModelGenerator;

import org.apache.commons.io.FileUtils;
import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.TransformationModule;
import org.mdtp.core.impl.FileConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WessbasModule implements TransformationModule {

	private static final Logger LOG = LoggerFactory.getLogger(WessbasModule.class);
	
	
	private FileConfigurationProperty tempDirectory = new FileConfigurationProperty("temporary folder", "Temporary Folder to use for storing temp files");

	private FileConfigurationProperty workloadFile = new FileConfigurationProperty("workload", "The file specifying the workload intensity for geenraitng the DSL instnace");
	
	private FileConfigurationProperty outputDslFile = new FileConfigurationProperty("output", "The output file which wil lstore the WESSBAS DSL instance.");
	
	private List<ConfigurationProperty<String>> allConfigs = Arrays.asList(
			tempDirectory,
			workloadFile,
			outputDslFile
			);
	
	@Override
	public List<? extends ConfigurationProperty<?>> getConfiguration() {
		return allConfigs;
	}

	@Override
	public void validateConfiguration(ErrorBuffer errors) {
		if(!tempDirectory.isPathValid()) {
			errors.addError("The given temp directory is not a valid file path!");
		} else {
			if(tempDirectory.getFile().get().exists()) {
				errors.addWarning("The given temp directory already exists and will be deleted!");
			}
		}
		
		if(!outputDslFile.isPathValid()) {
			errors.addError("The given output file is not a valid file path!");			
		} else {
			if(outputDslFile.getFile().get().exists()) {
				errors.addWarning("The given output file already exists and wil lbe overwritten!");		
			}
		}
		
		
		if(!workloadFile.isPathValid()) {
			errors.addError("The given workload intensity file is not a valid file path!");			
		} else {
			if(!workloadFile.getFile().get().exists()) {
				errors.addError("The given workload intensity file does not exist!");			
			}
		}
		
	}

	@Override
	public void execute(MonitoringDataSet monitoringData) {

		try {
			File tempDir = tempDirectory.getFile().get();
			String prefix = tempDir.getAbsolutePath() + File.separator;
			LOG.info("Using temp dir " + prefix);

			String sessionsFile = prefix+"sessions.dat";

			File behaviourDir = new File(prefix+"behaviour");
			String behaviourPath = behaviourDir.getAbsolutePath();
			String workloadIntensityFile = workloadFile.getFile().get().getAbsolutePath();
			
			
			if (tempDir.exists()) {
				FileUtils.deleteDirectory(tempDir);
			}
			FileUtils.forceMkdir(tempDir);

			LOG.info("Extracting sessions.dat file....");
			//Step 1: generate a sessions.dat file from the MDM
			FileOutputStream fout = FileUtils.openOutputStream(new File(sessionsFile));
			MDMToSessionsDatConverter converter = new MDMToSessionsDatConverter();	
			converter.convert(monitoringData.getRootEvents().stream(), fout);
			fout.close();


			LOG.info("Performing behavior clustering...");
			//step 2: execute behaviour clustering
			FileUtils.forceMkdir(behaviourDir);
			
			
			String[] cmdArgs = {
				"-i",""+sessionsFile+"",
				"-o",""+behaviourPath+"",
				"-c","kmeans",
				"-min","2",
				"-max","2",
				
			};
			
			BehaviorModelExtractor.main(cmdArgs);
			

			
			String[] dslArgs = {
				"-s",""+sessionsFile+"",
				"-w",""+workloadIntensityFile+"",
				"-b",""+behaviourPath+File.separator+"behaviormix.txt",
				"-o",""+outputDslFile.getFile().get().toURI().toString(),
			};

			LOG.info("Generating WESSBAS DSL instance...");
			M4jdslModelGenerator.main(dslArgs);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 

	}
	
	public static void main(String[] args) {
		WessbasModule mod = new WessbasModule();
		mod.tempDirectory.setValue("temp");
		mod.outputDslFile.setValue("myDsl.xmi");
		mod.workloadFile.setValue("workloadIntensity.properties");
		
		MonitoringDataSet mdm = readMdmFile("specJ.mdm");
		
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
