package org.mdtp.iobserve.pipeline;

import mdm.api.core.Event;
import mdm.api.core.MonitoringDataSet;
import teetime.framework.AbstractProducerStage;

/**
 * Producer stage.
 * Extracts all root events from a MDM instance and passes them one by one to the following stage.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class MDMEventProducerStage extends AbstractProducerStage<Event>{

	private MonitoringDataSet sourceMdm;
	
	@Override
	public void onStarting() throws Exception {
		if (sourceMdm == null) {
			throw new IllegalArgumentException("The source MDM has to be set before executing this stage!");
		}
		super.onStarting();
	}
	
	@Override
	protected synchronized void execute() {
		sourceMdm.getRootEvents().forEach(outputPort::send);
		terminate();
	}
	
	/**
	 * @param mdm the MDM from which the root events will be extracted
	 */
	public synchronized void setSourceMDM(MonitoringDataSet mdm) {
		sourceMdm = mdm;
	}

}
