package org.mdtp.iobserve.pipeline;

import mdm.api.core.MonitoringDataSet;

import org.eclipse.emf.common.util.URI;
import org.iobserve.analysis.ObservationConfiguration;
import org.iobserve.analysis.correspondence.CorrespondeceModelFactory;
import org.iobserve.analysis.correspondence.ICorrespondence;
import org.iobserve.analysis.filter.DeploymentEventTransformation;
import org.iobserve.analysis.filter.TEntryCallSequence;
import org.iobserve.analysis.filter.TEntryEventSequence;
import org.iobserve.analysis.filter.UndeploymentEventTransformation;
import org.iobserve.analysis.modelprovider.PcmModelSaver;
import org.iobserve.analysis.modelprovider.UsageModelProvider;

import teetime.framework.AnalysisConfiguration;
import teetime.framework.pipe.IPipeFactory;
import teetime.framework.pipe.PipeFactoryRegistry;
import teetime.framework.pipe.PipeFactoryRegistry.PipeOrdering;
import teetime.framework.pipe.PipeFactoryRegistry.ThreadCommunication;


/**
 * 
 * Configures the pipeline to correctly conenct the individual stages.
 * This class is an adapted version of the {@link ObservationConfiguration} class.
 * 
 * @author Jonas Kunz, advisors: Robert Heinrich, Christoph heger
 *
 */
public class PipelineConfiguration extends AnalysisConfiguration{

	private final PipeFactoryRegistry pipeFactoryRegistry = PipeFactoryRegistry.INSTANCE;
	
	//private MDMEventProducerStage producerStage;
	
	private final String correspondenceModelPath;
	private final String inputPCMRepoPath;
	private final String inputPCMUsageModelPath;
	private final String outputPCMUsageModelPath;
	private final MonitoringDataSet sourceMDM;
	
	

	/**
	 * Constructor. Takes the confiugration of the input data as arguments.
	 * 
	 * @param correspondenceModelPath file path to the correspondence model file
	 * @param inputPCMRepoPath file path to the input PCM repository model file
	 * @param inputPCMUsageModelPath file path to the input PCM usage model file
	 * @param outputPCMUsageModelPath file path to the output PCM usage model file
	 * @param mdm
	 */
	public PipelineConfiguration(String correspondenceModelPath, String inputPCMRepoPath, String inputPCMUsageModelPath, String outputPCMUsageModelPath, MonitoringDataSet mdm) {
		super();
		this.correspondenceModelPath = correspondenceModelPath;
		this.inputPCMRepoPath = inputPCMRepoPath;
		this.inputPCMUsageModelPath = inputPCMUsageModelPath;
		this.outputPCMUsageModelPath = outputPCMUsageModelPath;
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
		
		final ICorrespondence correspondenceModel = this.getCorrespondenceModel();

		// create filter

		final DeploymentEventTransformation deployment = new DeploymentEventTransformation(
				correspondenceModel);
		final UndeploymentEventTransformation undeployment = new UndeploymentEventTransformation(
				correspondenceModel);

		final TEntryCallSequence tEntryCallSequence = new TEntryCallSequence();

		// get the usage model provider and reset it
		final UsageModelProvider usageModelProvider = this.getUsageModelProvider();
		usageModelProvider.resetUsageModel();

		final TEntryEventSequence tEntryEventSequence = new TEntryEventSequence(
				correspondenceModel, usageModelProvider, this.getPcmModelSaver());
		
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
	

	/**
	 * Get the correspondence model
	 * @return instance of {@link ICorrespondence}
	 */
	private ICorrespondence getCorrespondenceModel() {
		final ICorrespondence model = CorrespondeceModelFactory.INSTANCE
				.createCorrespondenceModel(correspondenceModelPath,
						CorrespondeceModelFactory.INSTANCE.DEFAULT_OPERATION_SIGNATURE_MAPPER_2);
		return model;
	}

	/**
	 * Get the Model provider for the usage model
	 * @return instance of usage model provider
	 */
	private UsageModelProvider getUsageModelProvider() {
		final URI repositoryModelURI = URI.createFileURI(inputPCMRepoPath);
		final URI inputUsageModelURI = URI.createFileURI(inputPCMUsageModelPath);
		final UsageModelProvider provider = new UsageModelProvider(inputUsageModelURI, repositoryModelURI);

		return provider;
	}

	/**
	 * get the helper class to save PCM models
	 * @return instance of that class
	 */
	private PcmModelSaver getPcmModelSaver() {
		final URI outputUsageModelURI = URI.createFileURI(outputPCMUsageModelPath);
		return new PcmModelSaver(outputUsageModelURI);
	}

}
