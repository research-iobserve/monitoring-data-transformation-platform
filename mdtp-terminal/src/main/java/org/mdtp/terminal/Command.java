package org.mdtp.terminal;

/**
 * Class representing an executable command line command.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface Command {
	
	/**
	 * Returns the command the user has to type to execute this command.
	 * @return the unique name of the command.
	 */
	String getCommand();

	/**
	 * Executes the command using the given terminal.
	 * The temrinal can be used to parse further necessary information.
	 * @param terminal
	 */
	void execute(Terminal terminal);

	/**
	 * @return A human readable description of the purpose of this command.
	 */
	String getDescription();
}
