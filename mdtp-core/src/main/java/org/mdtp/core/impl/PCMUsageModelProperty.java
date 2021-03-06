package org.mdtp.core.impl;

import java.io.File;


/**
 * 
 * Configuration property for specifying a PCM usage model instance, stored in an XMI file.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class PCMUsageModelProperty extends FileConfigurationProperty {
	

	/**
	 * @param name the name of this property
	 * @param description a description for this property
	 */
	public PCMUsageModelProperty(String name, String description) {
		super(name, description);
	}

	@Override
	public boolean isPathValid() {
		return super.isPathValid() && !getFile().get().isDirectory() && getValue().get().toLowerCase().endsWith(".usagemodel");
	}

	/**
	 * Checks if the model can be loaded.
	 * @return true if the model was loadable.
	 */
	public boolean isModelLoadable() {
		if(isPathValid()) {
			File file = super.getFile().get();
			if(file.exists()) {
				//TODO: Check if the file can be loaded by the emf framework
				return true;
			}
		}
		return false;
	}
}