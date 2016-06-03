package org.mdtp.core.impl;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Optional;

import org.mdtp.core.ConfigurationProperty;

/**
 * 
 * Configuration property for specifying a single file. Uses a string value to reference the file path.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class FileConfigurationProperty implements ConfigurationProperty<String> {

	private String filePath;
	
	private String name;
	private final String description;
	
	/**
	 * Constructor. Takes the name and the description for this property as argumetns.
	 * @param name the unique name of this configuration property
	 * @param description a human-readable description of what this configuration property stands for
	 */
	public FileConfigurationProperty(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	
	public String getDescription() {
		return description;
	}

	public Class<String> getValueType() {
		return String.class;
	}

	public Optional<String> getValue() {
		return Optional.ofNullable(filePath);
	}

	public void setValue(String value) {
		filePath = value;
	}
	
	/**
	 * @return true, if the value of this property has the correct syntax of a file path
	 */
	public boolean isPathValid() {
		return checkPath();
	}

	private boolean checkPath() {
		if(filePath == null){
			return false;
		}
		try {
			Paths.get(filePath);
			return true;
		}catch( InvalidPathException ex) {
			return false;
		}
	}


	/**
	 * @return the file this property's value points to, if it is a correct path
	 */
	public Optional<File> getFile(){
		if(!checkPath()){
			return Optional.empty();
		}
		return Optional.of(new File(filePath));
	}

	public String getName() {
		return name;
	}
	
}