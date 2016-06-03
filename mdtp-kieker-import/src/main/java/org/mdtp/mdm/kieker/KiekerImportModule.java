package org.mdtp.mdm.kieker;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import mdm.api.core.MonitoringDataSet;
import mdm.dflt.impl.core.MonitoringDataSetImpl;

import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.ImportModule;
import org.mdtp.core.impl.FileConfigurationProperty;
import org.mdtp.mdm.kieker.passes.AbstractMDMUpdatePass;
import org.mdtp.mdm.kieker.passes.EventBasedTraceReconstructionPass;
import org.mdtp.mdm.kieker.passes.EventLocationPass;
import org.mdtp.mdm.kieker.passes.ExecutionBasedTraceReconstructionPass;
import org.mdtp.mdm.kieker.passes.IObserveEventsTranslationPass;
import org.mdtp.mdm.kieker.passes.WESSBASHttpInfoExtractionPass;

public class KiekerImportModule implements ImportModule{
	
	
	private FileConfigurationProperty inDir = new FileConfigurationProperty("inputDir", 
			"Directory containing one or more sub directories with Kieker measurement data.\n"
			+ "A directory will be loaded, if it contains a kieker.map file.\n"
			+ "All sub-directories of the given path will be searched.");
	
	List<FileConfigurationProperty> allProperties = Arrays.asList(
			inDir);
	@Override
	public List<? extends ConfigurationProperty<?>> getConfiguration() {
		return allProperties;
	}

	@Override
	public void validateConfiguration(ErrorBuffer errors) {
		Optional<File> file = inDir.getFile();
		if(inDir.isPathValid() && file.get().isDirectory() && file.get().exists()) {
			if(listMonitoringDataFolders().isEmpty()) {
				errors.addError("The path \"" + inDir.getValue().get()+"\" does not contain any Kieker monitoring data!");	
			}
		} else {
			errors.addError("The path \"" + inDir.getValue().get()+"\" does not point to a valid directory!");			
		}
	}

	@Override
	public MonitoringDataSet importMonitoringDataModel() {
		List<String> inputFolders = listMonitoringDataFolders();
		
		MonitoringDataSetImpl monitoringDataModel = new MonitoringDataSetImpl();
		
		List<AbstractMDMUpdatePass> passes = Arrays.asList(
				new ExecutionBasedTraceReconstructionPass(inputFolders),
				new EventBasedTraceReconstructionPass(inputFolders),
				new WESSBASHttpInfoExtractionPass(inputFolders),
				new IObserveEventsTranslationPass(inputFolders),
				new EventLocationPass(inputFolders)
		);
	
		for(AbstractMDMUpdatePass pass : passes) {
			pass.runAndWait(monitoringDataModel);
		}
		
		return monitoringDataModel;
	}
	
	private List<String> listMonitoringDataFolders(){
		Optional<File> file = inDir.getFile();
		if(inDir.isPathValid() && file.get().isDirectory() && file.get().exists()) {
			try {
				return Files.walk(file.get().toPath(),FileVisitOption.FOLLOW_LINKS)
				.filter(Files::isDirectory)
				.filter((p) -> {
					try{
						return 
								Files.list(p)
								.filter((f) -> !Files.isDirectory(f))
								.filter((f) -> f.getFileName().toString().equals("kieker.map"))
								.findAny().isPresent();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.map(Path::toString)
				.collect(Collectors.toList());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return Collections.emptyList();
		}
	}

}
