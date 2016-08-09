package org.mdtp.core.impl;

import java.util.Optional;

import org.mdtp.core.ConfigurationProperty;

/**
 * Default base implementation for generic ConfigurationProperties.
 * 
 * @author Jonas Kunz
 *
 * @param <T> the type of the value which cna be configured.
 */
public class DefaultConfigurationProperty<T> implements ConfigurationProperty<T> {

	private T value;
	private String name;
	private String description;
	
	private Class<T> valueType;
	
	
	/**
	 * @param name a short name of the proeprty (e.g. as a commandline parameter)
	 * @param description a more exhaustive description of what this proeprty is used for
	 * @param valueType the type of the property
	 */
	public DefaultConfigurationProperty(String name, String description, Class<T> valueType) {
		super();
		this.name = name;
		this.description = description;
		this.valueType = valueType;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Class<T> getValueType() {
		return valueType;
	}

	public Optional<T> getValue() {
		return Optional.ofNullable(value);
	}

	public void setValue(T value) {
		this.value = value;		
	}

}
