package org.mdtp.core;

import java.util.Optional;

/**
 * Represents a single configuration property.
 * This class provides the meta information about the property (e.g., name and description) and 
 * also acts as a storage for the value of the property.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 * @param <T> the type of value of this proeprty
 */
public interface ConfigurationProperty<T> {

	/**
	 * @return a unique name for this configuration property
	 */
	String getName();
	/**
	 * @return a description of what has to be configured here.
	 */
	String getDescription();
	
	/**
	 * @return the type of the value stored in this property
	 */
	Class<T> getValueType();
	
	/**
	 * @return an Optional contianing the configured value or an empty Optional if no value has been configured.
	 */
	Optional<T> getValue();
	
	/**
	 * @param value the value to set this property to
	 */
	void setValue(T value);

}
