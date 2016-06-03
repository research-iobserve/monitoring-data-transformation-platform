package mdm.api.deployment;

import java.util.Optional;

import mdm.api.core.TimedEvent;

/**
 * Generic event type representing the (un)deployment of a Servlet.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface ServletDeploymentEvent extends TimedEvent {
	Optional<String> getService();
	Optional<String> getContext();
	Optional<String> getDeploymentID();
}
