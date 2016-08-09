package org.mdtp.terminal.commands;

import mdm.api.core.MonitoringDataSet;

import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.TransformationModule;
import org.mdtp.terminal.Terminal;

import java.util.List;

/**
 * Command ofr executing an arbitary MDM transformation module.
 * @author Jonas Kunz
 *
 */
public class GenericMDMTransformationCommand extends AbstractConfiguringCommand{

	private TransformationModule transformer;
	private String transformationName;
	
	
	
	public GenericMDMTransformationCommand(TransformationModule transformer, String transformationName) {
		super();
		this.transformer = transformer;
		this.transformationName = transformationName;
	}

	public String getCommand() {
		return "transform-"+transformationName;
	}
	
	public String getDescription() {
		return "Transforms the loaded monitoring data using "+transformationName+".";
	}
	
	@SuppressWarnings("unchecked")
	public void execute(Terminal terminal) {
		terminal.printfln("Running monitoring data transformation " + transformationName +".");
		if(terminal.getCurrentMonitoringDataModel().isPresent()) {
			if(performConfiguration(terminal, (List<ConfigurationProperty<String>>)transformer.getConfiguration(), transformer::validateConfiguration)) {
	
				//run the importer
				terminal.printfln("Running transformation...");
				MonitoringDataSet mdm = terminal.getCurrentMonitoringDataModel().get();
				transformer.execute(mdm);
				terminal.printfln("Transformation finished!");		
			}		
			
		} else {
			terminal.printfln("No monitoring data model loaded! You have to load or import one before transforming it.");			
		}
	}

	
}
