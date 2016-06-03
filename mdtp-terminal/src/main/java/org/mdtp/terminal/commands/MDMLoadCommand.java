package org.mdtp.terminal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import mdm.api.core.MonitoringDataSet;
import mdm.dflt.impl.serialization.KryoMDMDeserializer;
import mdm.dflt.impl.serialization.MDMDeserializer;

import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.impl.FileConfigurationProperty;
import org.mdtp.terminal.Terminal;

public class MDMLoadCommand extends AbstractConfiguringCommand{

	@Override
	public String getCommand() {
		return "load-MDM";
	}

	@Override
	public String getDescription() {
		return "Loads a Monitoring Data Model stored in a file written by the store-MDM command from the hard disk.";
	}

	@Override
	public void execute(Terminal terminal) {
		if(terminal.getCurrentMonitoringDataModel().isPresent()) {
			terminal.printfln("The currently loaded MDM will be overwritten.");			
		}
		final FileConfigurationProperty outputFile = new FileConfigurationProperty("outFile", "The output file to store the MDM in.");
		Consumer<ErrorBuffer> validator = (errBuff) -> {
			if(!outputFile.isPathValid() || outputFile.getFile().get().isDirectory() | !outputFile.getFile().get().exists()) {
				errBuff.addError("The path \""+outputFile.getValue().get()+"\" is not a valid path to a mdm file!");
			}
		};
		if(this.performConfiguration(terminal, Arrays.asList(outputFile),validator)) {
			terminal.printfln("Reading file...");
			
			File file = outputFile.getFile().get();
			MDMDeserializer deserializer = new KryoMDMDeserializer();			
			try {
				
				deserializer.setSource(new FileInputStream(file));				
				MonitoringDataSet mdm  = (MonitoringDataSet) deserializer.readNext();
				if(mdm == null) {
					terminal.printfln("ERROR: %s", "The file did not contain a MDM!");					
				} else {
					terminal.setCurrentMonitoringDataModel(mdm);
					terminal.printfln("MDM loaded.");						
				}
			} catch (Exception e) {
				terminal.printfln("ERROR: %s", e.getMessage());
			}
		
		}
	}

}
