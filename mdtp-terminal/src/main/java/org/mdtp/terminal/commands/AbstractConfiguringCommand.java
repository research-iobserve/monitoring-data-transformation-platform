package org.mdtp.terminal.commands;

import java.util.List;
import java.util.function.Consumer;

import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.terminal.Command;
import org.mdtp.terminal.SimpleErrorBuffer;
import org.mdtp.terminal.Terminal;

public abstract class AbstractConfiguringCommand implements Command{
	
	protected boolean performConfiguration(Terminal terminal, List<ConfigurationProperty<String>> propertiesToConfigure, Consumer<ErrorBuffer> configurationValidator) {

		terminal.printfln("To abort the execution of the command, type \"q\" instead of the arguments asked below.");
		for(ConfigurationProperty<String> prop : propertiesToConfigure) {
			terminal.printfln("Please enter the next argument: %s", prop.getDescription());
			terminal.printf("value: ");
			String input = terminal.readString();
			if(input.equalsIgnoreCase("Q")) {
				terminal.printfln("Aborting command.");
				return false;
			} else {
				prop.setValue(input);				
			}
		}
		
		//Validate the entered configuration
		SimpleErrorBuffer errorBuf = new SimpleErrorBuffer();
		configurationValidator.accept(errorBuf);
		errorBuf.getMessages().ifPresent((msg) -> terminal.printfln(msg));
		if(errorBuf.hasErrors()) {
			terminal.printfln("Aborting command execution due to errors in the configuration.");
			return false;
		} else if(errorBuf.hasWarnings()) {
			terminal.printfln("The configuration contains warnings, do you want to continue?");
			if(!terminal.readYesNo()) {
				return false;				
			}
		}
		return true;
	}

}
