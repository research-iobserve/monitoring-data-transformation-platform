package mdm.dflt.impl.serialization;

import mdm.dflt.impl.core.EventSubTraceImpl;
import mdm.dflt.impl.core.MonitoringDataSetImpl;
import mdm.dflt.impl.core.UnmonitoredEventImpl;
import mdm.dflt.impl.deployment.EJBDeployedEventImpl;
import mdm.dflt.impl.deployment.EJBUndeployedEventImpl;
import mdm.dflt.impl.deployment.ServletDeployedEventImpl;
import mdm.dflt.impl.deployment.ServletUndeployedEventImpl;
import mdm.dflt.impl.http.HTTPRequestIssueImpl;
import mdm.dflt.impl.http.HTTPRequestReceivedEventImpl;
import rocks.cta.dflt.impl.serialization.realizations.KryoCTASerializationBase;

import com.esotericsoftware.kryo.Kryo;

/**
 * base class for Kryo serialization.
 * Register all serializabel classes.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class KryoMDMSerializationBase extends KryoCTASerializationBase{

	
	
	/**
	 * Constructor. Initializes kryo.
	 */
	public KryoMDMSerializationBase() {
		super();
		Kryo kryo = getKryoInstance();

		kryo.register(MonitoringDataSetImpl.class);
		kryo.register(EventSubTraceImpl.class);
		kryo.register(UnmonitoredEventImpl.class);
		
		kryo.register(ServletDeployedEventImpl.class);
		kryo.register(ServletUndeployedEventImpl.class);
		kryo.register(EJBDeployedEventImpl.class);
		kryo.register(EJBUndeployedEventImpl.class);

		kryo.register(HTTPRequestIssueImpl.class);
		kryo.register(HTTPRequestReceivedEventImpl.class);
	}

}
