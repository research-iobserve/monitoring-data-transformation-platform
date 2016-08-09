package org.mdtp.terminal.commands;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mdtp.terminal.Command;
import org.mdtp.terminal.Terminal;

import mdm.api.core.MonitoringDataSet;

/**
 * 
 * Command for printing information about the currently laoded MDM.
 * @author Jonas Kunz
 *
 */
public class StatusCommand implements Command{

	@Override
	public String getCommand() {
		return "status";
	}

	@Override
	public void execute(Terminal terminal) {
		if(terminal.getCurrentMonitoringDataModel().isPresent()) {
			MonitoringDataSet mdm = terminal.getCurrentMonitoringDataModel().get();
			terminal.printfln("The loaded MDM contains a total of %d events referring to %d traces.", mdm.getRootEvents().size(), mdm.getTraces().size());
			terminal.printfln("The events have the following distribution:");
			
			Map<String,Long> eventCounts = mdm.getRootEvents().stream()
			.map((e) -> e.getClass().getSimpleName())
			.collect(
					Collectors.groupingBy(Function.identity(), Collectors.counting()));
			
			eventCounts.forEach((name,count) -> terminal.printfln("%s - %d", name, count));
			
		} else {
			terminal.printfln("Currently no Monitoring Data Model is loaded.");
		}
	}

	@Override
	public String getDescription() {
		return "Prints information about the currently loaded Monitoring Data Model.";
	}

}
