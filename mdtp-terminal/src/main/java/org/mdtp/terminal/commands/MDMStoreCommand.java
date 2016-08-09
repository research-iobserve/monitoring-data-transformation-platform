package org.mdtp.terminal.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import mdm.dflt.impl.serialization.KryoMDMSerializer;
import mdm.dflt.impl.serialization.MDMSerializer;

import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.impl.FileConfigurationProperty;
import org.mdtp.terminal.Terminal;

/**
 * Stores the loaded MDM instance on the hard disk.
 * @author Jonas Kunz
 *
 */
public class MDMStoreCommand extends AbstractConfiguringCommand{

	@Override
	public String getCommand() {
		return "store-MDM";
	}

	@Override
	public String getDescription() {
		return "Stores the loaded Monitoring Data Model into a file on the hard disk.";
	}

	@Override
	public void execute(Terminal terminal) {
		if(terminal.getCurrentMonitoringDataModel().isPresent()) {
			
			final FileConfigurationProperty outputFile = new FileConfigurationProperty("outFile", "The output file to store the MDM in.");
			Consumer<ErrorBuffer> validator = (errBuff) -> {
				if(!outputFile.isPathValid() || outputFile.getFile().get().isDirectory()) {
					errBuff.addError("The path \""+outputFile.getValue().get()+"\" is not a valid path to a file!");
				} else if (outputFile.getFile().get().exists()) {
					errBuff.addWarning("The file \""+outputFile.getValue().get()+"\" already exists and will be overwritten.");
				}
			};
			if(this.performConfiguration(terminal, Arrays.asList(outputFile),validator)) {
				terminal.printfln("Writing output file...");
				File result = outputFile.getFile().get();
				try {
					if(result.exists()) {
						result.delete();
					}
					result.createNewFile();
								
					MDMSerializer serializer = new KryoMDMSerializer();
					
					serializer.prepare(new FileOutputStream(result));
					serializer.writeMonitoringDataSet(terminal.getCurrentMonitoringDataModel().get());
					serializer.close();
					terminal.printfln("done.");
				} catch (IOException e) {
					terminal.printfln("ERROR: %s", e.getMessage());
				}
			}
			
		} else {
			terminal.printfln("Currenty there is no Monitoring Data Model loaded.\nYou have to import or load one before it can be stored.");
		}
	}

}
