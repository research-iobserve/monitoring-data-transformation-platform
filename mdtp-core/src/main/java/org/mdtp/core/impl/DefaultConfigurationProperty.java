package org.mdtp.core.impl;

import java.util.Optional;

import org.mdtp.core.ConfigurationProperty;

public class DefaultConfigurationProperty<T> implements ConfigurationProperty<T> {

	private T value;
	private String name;
	private String description;
	
	private Class<T> valueType;
	
	
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
