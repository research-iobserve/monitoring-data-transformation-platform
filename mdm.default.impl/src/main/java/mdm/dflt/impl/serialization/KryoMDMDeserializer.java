package mdm.dflt.impl.serialization;

import java.io.InputStream;

import mdm.api.core.MonitoringDataSet;
import mdm.dflt.impl.core.MonitoringDataSetImpl;
import rocks.cta.api.core.Trace;
import rocks.cta.dflt.impl.core.TraceImpl;
import rocks.cta.dflt.impl.serialization.CTADeserializer;

import com.esotericsoftware.kryo.io.Input;

/**
 * Kryo-based deserialization of MDM instances.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class KryoMDMDeserializer extends KryoMDMSerializationBase implements MDMDeserializer {

	/**
	 * Source for deserialization.
	 */
	private Input input;

	/**
	 * Constructor.
	 */
	public KryoMDMDeserializer() {
		super();
	}

	@Override
	public void setSource(InputStream inStream) {
		input = new Input(inStream);

	}


	@Override
	public void close() {
		input.close();

	}

	@Override
	public MonitoringDataSet readNext() {
		if (input.eof()) {
			return null;
		}
		return (MonitoringDataSet) getKryoInstance().readObject(input, MonitoringDataSetImpl.class);
	}

}
