package org.mdtp.terminal;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import mdm.api.core.MonitoringDataSet;
import mdm.dflt.impl.serialization.KryoMDMDeserializer;
import mdm.dflt.impl.serialization.MDMDeserializer;

import org.mdtp.core.ConfigurationProperty;
import org.mdtp.core.ErrorBuffer;
import org.mdtp.iobserve.IObserveModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class iObserveTest {

	private static final Logger LOG = LoggerFactory.getLogger(iObserveTest.class);
	
	
	public static void main(String[] args) {
		
		Map<String,String> config = new HashMap<>();
		config.put("inPcmRepo", "C:/Users/JKU/Desktop/thesis/thesis_git/iobserve-analysis/org.iobserve.analysis/res/test/modells/PCMInput/cocome.repository");
		config.put("inPcmUsage", "C:/Users/JKU/Desktop/thesis/thesis_git/iobserve-analysis/org.iobserve.analysis/res/test/modells/PCMInput/cocome.usagemodel");
		config.put("outPcmUsage", "test-out.usagemodel");
		config.put("correspondences", "C:\\Users\\JKU\\Desktop\\thesis\\thesis_git\\iobserve-analysis\\org.iobserve.analysis\\res\\test\\rac\\mapping_fake.xml");
		
		IObserveModule mod = new IObserveModule();
		mod.getConfiguration().forEach((c) -> ((ConfigurationProperty<String>)c).setValue(config.get(c.getName())));
		
		mod.validateConfiguration(new ErrorBuffer() {
			
			@Override
			public void addWarning(String warningMessage) {
				LOG.warn(warningMessage);
			}
			
			@Override
			public void addError(String errorMsg) {
				LOG.error(errorMsg);
			}
		});
		
		
		//File file = new File("myMDM.mdm");
		File file = new File("myMDM.mdm");
		MDMDeserializer deserializer = new KryoMDMDeserializer();			
		try {
			
			deserializer.setSource(new FileInputStream(file));				
			MonitoringDataSet mdm  = (MonitoringDataSet) deserializer.readNext();
			
			mod.execute(mdm);
			
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
		}
	}

}
