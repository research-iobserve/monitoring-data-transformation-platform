package org.mdtp.core;

/**
 * A consumer recieving errors and warnings.
 * Errors are considered as failures while warnings may be ignored.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface ErrorBuffer {

	/**
	 * @param warningMessage the message of the warning
	 */
	public void addWarning(String warningMessage);
	
	/**
	 * @param errorMessage the message of the error
	 */
	public void addError(String errorMessage);
	
}
