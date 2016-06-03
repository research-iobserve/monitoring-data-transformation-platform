package mdm.dflt.impl.serialization;

import java.io.OutputStream;

import mdm.api.core.MonitoringDataSet;


/**
 * Superclass for all serializers for the MDM default implementation.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface MDMSerializer {

	
	/**
	 * @param outStream the output stream t owrite the data to
	 */
	void prepare(OutputStream outStream);

	
	/**
	 * @param mdm the MDM isntance to write out
	 */
	void writeMonitoringDataSet(MonitoringDataSet mdm);
	
	/**
	 * Closes the underlying data drain.
	 */
	void close();
}
