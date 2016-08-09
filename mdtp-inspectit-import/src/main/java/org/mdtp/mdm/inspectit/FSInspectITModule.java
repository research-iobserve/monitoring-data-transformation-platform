package org.mdtp.mdm.inspectit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdm.api.core.MonitoringDataSet;
import mdm.api.http.HTTPRequestReceivedEvent;

import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.core.ImportModule;
import org.mdtp.core.impl.FileConfigurationProperty;
import org.mdtp.mdm.inspectit.fs.InvocationSequences;
import org.mdtp.mdm.inspectit.fs.SerializerWrapper;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;

/**
 * Filesystem (FS) based inspectIT data importing.
 * Uses the Version-dependent Storage format to read inpsectIT storage data from the harddisk without any CMR interaction.
 * 
 * @author Jonas Kunz
 *
 */
public class FSInspectITModule implements ImportModule {
	
	/**
	 * The root folder of the inspectit storage to import.
	 */
	private FileConfigurationProperty storageFolder = new FileConfigurationProperty("storageFolder", "The directory of the storage to import");

	private List<ConfigurationProperty<String>> props = Arrays.asList(storageFolder);

	@Override
	public List<? extends ConfigurationProperty<?>> getConfiguration() {
		return props;
	}

	@Override
	public void validateConfiguration(ErrorBuffer errors) {
		if(!storageFolder.isPathValid()) {
			errors.addError("\""+storageFolder.getValue()+"\" is not a valid path!");
		} else {
			File file = storageFolder.getFile().get();
			if(!file.exists() || ! file.isDirectory()) {
				errors.addError("\""+storageFolder.getValue()+"\" is not a valid, existing directory!");				
			}
		}
	}

	@Override
	public MonitoringDataSet importMonitoringDataModel() {

		SerializerWrapper wrap;
		InvocationSequences seq;
		try {
			wrap = new SerializerWrapper();
			seq = wrap.readInvocationSequencesFromDir(storageFolder.getValue().get());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
		
		Map<Long,MethodIdent> methods = new HashMap<>();
		for(MethodIdent ident : seq.getPlatformIdent().getMethodIdents()) {
			methods.put(ident.getId(), ident);
		}
		
		InvocationSequencesTranslator translator = new InvocationSequencesTranslator();
		return translator.translate(methods,  seq.getPlatformIdent(), seq.getInvocationSequences());
	}

	public FileConfigurationProperty getStorageFolder() {
		return storageFolder;
	}
	

}
