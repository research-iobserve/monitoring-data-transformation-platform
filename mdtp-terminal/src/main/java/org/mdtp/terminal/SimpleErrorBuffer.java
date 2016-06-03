package org.mdtp.terminal;

import java.util.Optional;

import org.mdtp.core.ErrorBuffer;

/**
 * An error buffer which backs the error messages recieved by a string buffer.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class SimpleErrorBuffer implements ErrorBuffer{
	
	private StringBuffer buf = new StringBuffer();
	
	private boolean hasErrors = false;
	private boolean hasWarnings = false;

	@Override
	public void addWarning(String warningMessage) {
		hasWarnings = true;
		//add linebreak if necessary
		if(buf.length() != 0) {
			buf.append("\n");
		}
		
		buf.append("WARNING: ");
		buf.append(warningMessage);
	}

	@Override
	public void addError(String errorMessage) {
		hasErrors = true;
		//add linebreak if necessary
		if(buf.length() != 0) {
			buf.append("\n");
		}
		buf.append("ERROR: ");
		buf.append(errorMessage);
	}

	public boolean hasErrors() {
		return hasErrors;
	}
	
	
	public Optional<String> getMessages(){
		if(buf.length() == 0) {
			return Optional.empty();
		} else {
			return Optional.of(buf.toString());
		}
	}

	public boolean hasWarnings() {
		return hasWarnings;
	}
	
	

}
