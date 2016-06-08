/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package org.mdtp.mdm.kieker.filters;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import mdm.dflt.impl.core.AbstractEvent;
import mdm.dflt.impl.core.MonitoringDataSetImpl;
import mdm.dflt.impl.deployment.AbstractEJBDeploymentEvent;
import mdm.dflt.impl.deployment.AbstractServletDeploymentEvent;
import mdm.dflt.impl.deployment.EJBDeployedEventImpl;
import mdm.dflt.impl.deployment.EJBUndeployedEventImpl;
import mdm.dflt.impl.deployment.ServletDeployedEventImpl;
import mdm.dflt.impl.deployment.ServletUndeployedEventImpl;

import org.iobserve.common.record.EJBDeploymentEvent;
import org.iobserve.common.record.IDeploymentRecord;
import org.iobserve.common.record.IUndeploymentRecord;
import org.iobserve.common.record.ServletDeploymentEvent;

/**
 * This fitler takes the records form the iObserve Kieker extensions and translates them into MDM events.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
@Plugin(description = "Filter for extracting iOsberve deployment events to a mdm instance", outputPorts = {}, configuration = {})
public final class IObserveEventsTranslationFilter extends AbstractFilterPlugin {

	/** The name of the input port for incoming events. */
	public static final String INPUT_PORT_NAME_EVENTS = "receivedEvents";

	public static final String KIEKER_TRACE_ID_KEY = "KIEKER_TRACE_ID";

	/**
	 * Reference to the MDM which will be updated when records are converted.
	 */
	private MonitoringDataSetImpl mdm;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param configuration
	 *            The configuration for this component.
	 * @param projectContext
	 *            The project context for this component.
	 */
	public IObserveEventsTranslationFilter(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
	}

	@Override
	public boolean init() {
		return super.init();
	}

	@Override
	public final void terminate(final boolean error) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();
		// We reverse the if-decisions within the constructor.
		return configuration;
	}

	// private static int runs = 1;

	/**
	 * This method converts incoming iObserve event records to MDM events.
	 * 
	 * @param object
	 *            The new object.
	 */
	@InputPort(name = INPUT_PORT_NAME_EVENTS, description = "Receives incoming objects to be logged and forwarded", eventTypes = { Object.class })
	public final void inputEvent(final Object object) {
		AbstractEvent resultEvent = null;
		
		if (object instanceof ServletDeploymentEvent) {
			resultEvent = translateServletDeploymentEvent((ServletDeploymentEvent) object);
		} else if (object instanceof EJBDeploymentEvent) {
			resultEvent = translateEJBDeploymentEvent((EJBDeploymentEvent)object);
		}
		if (resultEvent != null) {
			synchronized (mdm) {
				mdm.addRootEvent(resultEvent);
			}
		}
	}

	/**
	 * Translates EJB deployment events.
	 * 
	 * @param inEvent
	 * @return
	 */
	private AbstractEJBDeploymentEvent translateEJBDeploymentEvent(EJBDeploymentEvent inEvent) {

		AbstractEJBDeploymentEvent resultEvent;
		if (inEvent instanceof IDeploymentRecord) {
			resultEvent = new EJBDeployedEventImpl();
		} else if (inEvent instanceof IUndeploymentRecord) {
			resultEvent = new EJBUndeployedEventImpl();
		} else {
			throw new RuntimeException("unknown EJB event!");
		}
		resultEvent.setContext(inEvent.getContext());
		resultEvent.setDeploymentID(inEvent.getDeploymentId());
		resultEvent.setTimestamp(inEvent.getTimestamp());
		resultEvent.setService(inEvent.getSerivce());
		return resultEvent;
	}

	/**
	 * Translates Servlet deployment events.
	 * 
	 * @param inEvent
	 * @return
	 */
	private AbstractServletDeploymentEvent translateServletDeploymentEvent(ServletDeploymentEvent inEvent) {

		AbstractServletDeploymentEvent resultEvent;
		if (inEvent instanceof IDeploymentRecord) {
			resultEvent = new ServletDeployedEventImpl();
		} else if (inEvent instanceof IUndeploymentRecord) {
			resultEvent = new ServletUndeployedEventImpl();
		} else {
			throw new RuntimeException("unknown Servlet event!");
		}
		resultEvent.setContext(inEvent.getContext());
		resultEvent.setDeploymentID(inEvent.getDeploymentId());
		resultEvent.setService(inEvent.getSerivce());
		resultEvent.setTimestamp(inEvent.getTimestamp());
		return resultEvent;
	}
	


	/**
	 * Passes the monitoring data set into which the generated events will be written.
	 * @param outputMdm the mdm to use
	 */
	public void setMonitoringDataSet(MonitoringDataSetImpl outputMdm) {
		mdm = outputMdm;
	}
}