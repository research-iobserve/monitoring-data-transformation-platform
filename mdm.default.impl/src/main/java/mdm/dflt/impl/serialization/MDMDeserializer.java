package mdm.dflt.impl.serialization;

import java.io.InputStream;

import mdm.api.core.MonitoringDataSet;

/**
 * Superclass for all deserializers for the MDM default implementation.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface MDMDeserializer {


	/**
	 * @param inStream sets the source from twhere teh data is read.
	 */
	void setSource(InputStream inStream);

	/**
	 * @return the next MDM instance, or null if the EOF is reached
	 */
	MonitoringDataSet readNext();
	
	/**
	 * Closes the underlying data source.
	 */
	void close();
}
