package mdm.api.deployment;

/**
 * Event representing the undeployment of a Servlet.
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public interface ServletUndeployedEvent extends UndeployedEvent,
		ServletDeploymentEvent {

}
