package mdm.api.deployment;

import java.util.Optional;

import mdm.api.core.TimedEvent;

/**
 * Generic event type representing the (un)deployment of an EJB.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface EJBDeploymentEvent extends TimedEvent {
	Optional<String> getDeploymentID();
	Optional<String> getContext();

}
