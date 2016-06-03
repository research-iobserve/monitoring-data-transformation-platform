package org.mdtp.terminal.commands;

import mdm.api.core.MonitoringDataSet;

import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ImportModule;
import org.mdtp.terminal.Terminal;

import java.util.List;

public class GenericMDMImportCommand extends AbstractConfiguringCommand{

	private ImportModule importer;
	private String monitoringToolName;
	
	
	
	public GenericMDMImportCommand(ImportModule importer, String monitoringToolName) {
		super();
		this.importer = importer;
		this.monitoringToolName = monitoringToolName;
	}

	public String getCommand() {
		return "import-"+monitoringToolName;
	}
	
	public String getDescription() {
		return "Imports monitoring data of "+monitoringToolName+" into the in-memory Monitoring Data Model.\n"
				+"The previously loaded data will be overwritten.";
	}
	
	@SuppressWarnings("unchecked")
	public void execute(Terminal terminal) {
		terminal.printfln("Running monitoring data importer for " + monitoringToolName +" data.\n"
				+ "Any loaded Monitoring Data Model will be overwritten.\n");
		
		
		if(performConfiguration(terminal, (List<ConfigurationProperty<String>>)importer.getConfiguration(), importer::validateConfiguration)) {

			//run the importer
			terminal.printfln("Running importer...");
			MonitoringDataSet mdm = importer.importMonitoringDataModel();
			terminal.setCurrentMonitoringDataModel(mdm);
			terminal.printfln("Monitoring Data Model successfully imported!");		
		}		
		
	}

	
}
