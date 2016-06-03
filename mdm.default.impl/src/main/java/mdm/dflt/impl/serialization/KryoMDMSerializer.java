package mdm.dflt.impl.serialization;

import java.io.OutputStream;

import mdm.api.core.MonitoringDataSet;
import mdm.dflt.impl.core.MonitoringDataSetImpl;
import rocks.cta.api.core.Trace;
import rocks.cta.dflt.impl.core.TraceImpl;
import rocks.cta.dflt.impl.serialization.CTASerializer;

import com.esotericsoftware.kryo.io.Output;

/**
 * Serializer for Kryo-based serialization.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class KryoMDMSerializer extends KryoMDMSerializationBase implements MDMSerializer {

	private Output output;

	public KryoMDMSerializer() {
		super();
	}

	@Override
	public void prepare(OutputStream outStream) {
		output = new Output(outStream);

	}

	
	@Override
	public void writeMonitoringDataSet(MonitoringDataSet mdm) {
		if (!(mdm instanceof MonitoringDataSetImpl)) {
			throw new IllegalArgumentException("This serializer can only serialize instances of " + MonitoringDataSetImpl.class.getName());
		}
		getKryoInstance().writeObject(output, mdm);
	}


	@Override
	public void close() {
		output.close();

	}

}
