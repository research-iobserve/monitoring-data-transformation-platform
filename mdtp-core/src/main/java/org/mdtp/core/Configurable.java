package org.mdtp.core;

import java.util.List;

/**
 * Represents an element which can be configured by a set of {@link ConfigurationProperty}s.
 * If exposes the configuration properties and facilities to check if the configuration is correct.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface Configurable {
	
	/**
	 * Queries the available ConfigurationProperties.
	 * The elements in the List provide the meta information for the configuration 
	 * and also act as storage for the actual configured value.
	 * @return a list of all configuration properties
	 */
	List<? extends ConfigurationProperty<?>> getConfiguration();
	
	/**
	 * Checks whether the configuration stored in the ConfigurationProperties is correct.
	 * If warnings or errors arise from the configuration, they are passed to the given ErrorBuffer.
	 * 
	 * @param errors the buffer to output errors and warnings regarding the ocnfiguration to
	 */
	void validateConfiguration(ErrorBuffer errors);
}
