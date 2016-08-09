package org.mdtp.terminal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.mdtp.iobserve.IObserveModule;
import org.mdtp.mdm.inspectit.RestInspectITModule;
import org.mdtp.mdm.kieker.KiekerImportModule;
import org.mdtp.terminal.commands.GenericMDMImportCommand;
import org.mdtp.terminal.commands.GenericMDMTransformationCommand;
import org.mdtp.terminal.commands.MDMLoadCommand;
import org.mdtp.terminal.commands.MDMStoreCommand;
import org.mdtp.terminal.commands.StatusCommand;
import org.mdtp.wessbas.WessbasModule;

public class Main {

	/**
	 * The set of available commands.
	 */
	private static List<Command> commands = Arrays.asList(
			new GenericMDMImportCommand(new KiekerImportModule(), "Kieker"),
			new GenericMDMImportCommand(new RestInspectITModule(), "inspectIT-rest"),
			new StatusCommand(),
			new MDMStoreCommand(),
			new MDMLoadCommand(),
			new GenericMDMTransformationCommand(new IObserveModule(), "iObserve"),
			new GenericMDMTransformationCommand(new WessbasModule(), "WESSBAS")
			);
	
	public static void main(String[] args) {
		Terminal terminal = new Terminal();
		terminal.printfln("Welcome to the Monitoring Data Transformation Platform Terminal! Type \"help\" for help or \"q\" to exit.");
		String inCmd;
		terminal.printf("command: ");
		while(!(inCmd = terminal.readString()).equalsIgnoreCase("q")) {
			if(inCmd.equalsIgnoreCase("help")) {
				printHelp(terminal);
			} else {
				final String inCmdf = inCmd;
				Optional<Command> cmd = commands.stream().filter((c) -> inCmdf.equalsIgnoreCase(c.getCommand())).findAny();
				if(cmd.isPresent()) {
					cmd.get().execute(terminal);
				} else {
					terminal.printfln("No such command: \"%s\"", inCmd);
				}
			}
			terminal.printf("command: ");
			
		}
		terminal.printfln("Exiting application.");
	}
	
	/**
	 * Prints the help information about all commands
	 * @param terminal
	 */
	private static void printHelp(Terminal terminal){
		terminal.printfln("\"q\"");
		terminal.printfln("Exit the application.");
		terminal.printfln("");
		for(Command cmd : commands) {
			terminal.printfln("\"%s\"", cmd.getCommand());
			terminal.printfln( cmd.getDescription());
			terminal.printfln("");
		}
	}
	
}
