package org.mdtp.iobserve.pipeline;

import mdm.api.core.MonitoringDataSet;

import org.iobserve.analysis.ObservationConfiguration;
import org.iobserve.analysis.filter.TDeployment;
import org.iobserve.analysis.filter.TEntryCallSequence;
import org.iobserve.analysis.filter.TEntryEventSequence;
import org.iobserve.analysis.filter.TUndeployment;

import teetime.framework.AnalysisConfiguration;
import teetime.framework.pipe.IPipeFactory;
import teetime.framework.pipe.PipeFactoryRegistry;
import teetime.framework.pipe.PipeFactoryRegistry.PipeOrdering;
import teetime.framework.pipe.PipeFactoryRegistry.ThreadCommunication;


/**
 * 
 * Configures the pipeline to correctly connect the individual stages.
 * This class is an adapted version of the {@link ObservationConfiguration} class.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class PipelineConfiguration extends AnalysisConfiguration{

	private final PipeFactoryRegistry pipeFactoryRegistry = PipeFactoryRegistry.INSTANCE;
	
	//private MDMEventProducerStage producerStage;
	
	private final MonitoringDataSet sourceMDM;
	
	

	/**
	 * Constructor. Takes the confiugration of the input data as arguments.
	 * 
	 * @param mdm
	 */
	public PipelineConfiguration(MonitoringDataSet mdm) {
		super();
		this.sourceMDM = mdm;
		configure();
	}



	/**
	 * performs the connection of the iObserve pipeline stages
	 */
	private void configure() {
		
		MDMEventProducerStage producerStage = new MDMEventProducerStage();
		producerStage.setSourceMDM(sourceMDM);
		
		MDMEventSwitchStage eventSwitch = new MDMEventSwitchStage();
		DeploymentEventBackTranslationStage deploymentTranslation = new DeploymentEventBackTranslationStage();
		EntryCallTranslationStage entryCallTranslation = new EntryCallTranslationStage();
		
		// create filter

		final TDeployment deployment = new TDeployment();
		final TUndeployment undeployment = new TUndeployment();

		final TEntryCallSequence tEntryCallSequence = new TEntryCallSequence();

		// get the usage model provider and reset it
	//	final UsageModelProvider usageModelProvider = this.getUsageModelProvider();
	//	usageModelProvider.resetUsageModel();

		final TEntryEventSequence tEntryEventSequence = new TEntryEventSequence();
		
		final IPipeFactory factory = this.pipeFactoryRegistry.getPipeFactory(
				ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false);
		
		factory.create(producerStage.getOutputPort(), eventSwitch.getInputPort());
		factory.create(eventSwitch.getSessionAwareEventsOutputPort(), entryCallTranslation.getInputPort());
		factory.create(eventSwitch.getOtherEventsOutputPort(), deploymentTranslation.getInputPort());	
		
		factory.create(deploymentTranslation.getDeploymentOutputPort(), deployment.getInputPort());
		factory.create(deploymentTranslation.getUndeploymentOutputPort(), undeployment.getInputPort());

		factory.create(entryCallTranslation.getOutputPort(), tEntryCallSequence.getInputPort());
		factory.create(tEntryCallSequence.getOutputPort(), tEntryEventSequence.getInputPort());
		
		addThreadableStage(producerStage);
	}
	
}
