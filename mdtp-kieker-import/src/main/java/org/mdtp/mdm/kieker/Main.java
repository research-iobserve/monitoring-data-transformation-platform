package org.mdtp.mdm.kieker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.imageio.stream.FileImageInputStream;

import rocks.cta.api.core.Trace;
import kieker.analysis.AnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.flow.EventRecordTraceReconstructionFilter;
import kieker.analysis.plugin.filter.select.TimestampFilter;
import kieker.analysis.plugin.filter.select.TraceIdFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.executionRecordTransformation.ExecutionRecordTransformationFilter;
import kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter;
import kieker.tools.traceAnalysis.filter.sessionReconstruction.SessionReconstructionFilter;
import kieker.tools.traceAnalysis.filter.traceReconstruction.TraceReconstructionFilter;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import mdm.api.core.MonitoringDataSet;
import mdm.api.http.SessionAwareEvent;
import mdm.dflt.impl.core.MonitoringDataSetImpl;
import mdm.dflt.impl.serialization.KryoMDMDeserializer;
import mdm.dflt.impl.serialization.KryoMDMSerializer;
import mdm.dflt.impl.serialization.MDMDeserializer;
import mdm.dflt.impl.serialization.MDMSerializer;

import org.diagnoseit.spike.kieker.trace.source.CTAFilter;
import org.iobserve.common.record.EJBDeployedEvent;
import org.mdtp.mdm.kieker.filters.MDMTraceCreationFilter;
import org.mdtp.mdm.kieker.passes.AbstractMDMUpdatePass;
import org.mdtp.mdm.kieker.passes.EventBasedTraceReconstructionPass;
import org.mdtp.mdm.kieker.passes.EventLocationPass;
import org.mdtp.mdm.kieker.passes.ExecutionBasedTraceReconstructionPass;
import org.mdtp.mdm.kieker.passes.IObserveEventsTranslationPass;
import org.mdtp.mdm.kieker.passes.WESSBASHttpInfoExtractionPass;

public class Main {
	
	private static final String OUTPUT_FILE = "MonitoringDataModel.mdm";
	
	//static final String path = "C:\\Users\\JKU\\Desktop\\thesis\\measurement data\\wessbas-withservletinfo";
	static final String path = "C:\\Users\\JKU\\Desktop\\thesis\\measurement data\\cocome\\data";
	//static final String path = "C:\\Users\\JKU\\Desktop\\thesis\\measurement data\\cocome\\shortened";
	//static final String path = "C:\\Users\\JKU\\Desktop\\thesis\\measurement data\\cocome\\new\\data";
	//static final String path =  "C:\\Users\\JKU\\Desktop\\thesis\\measurement data\\wessbass-specJ\\specj_18min_4min_rampup_rampdown_800_user_25p_50b_25m";
	
	static final List<String> disallowedFolders = Arrays.asList(
			//"kieker-20160309-101549259-UTC-logicnode-KIEKER",
			//"kieker-20160309-101618094-UTC-webnode-KIEKER",
			//"kieker-20160309-101050595-UTC-srvadapter-KIEKER"			
			);

	private static List<String> listFolders(){
		try {
			return Files.walk(new File(path).toPath(),FileVisitOption.FOLLOW_LINKS)
			.filter(Files::isDirectory)
			.filter((p) -> {
				try{
					return 
							Files.list(p)
							.filter((f) -> !Files.isDirectory(f))
							.filter((f) -> f.getFileName().toString().equals("kieker.map")).findAny().isPresent();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.filter((p) -> !disallowedFolders.contains(p.getFileName().toString()))
			.map(Path::toString).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static void main(String[] args){
	
		List<String> inputFolders = listFolders();
		
		MonitoringDataSetImpl monitoringDataModel = new MonitoringDataSetImpl();
		
		List<AbstractMDMUpdatePass> passes = Arrays.asList(
				new ExecutionBasedTraceReconstructionPass(inputFolders),
				new EventBasedTraceReconstructionPass(inputFolders),
				new WESSBASHttpInfoExtractionPass(inputFolders),
				new IObserveEventsTranslationPass(inputFolders),
				new EventLocationPass(inputFolders)
		);
	
		for(AbstractMDMUpdatePass pass : passes) {
			System.out.println("Running "+pass.getClass().getSimpleName()+".");
			pass.runAndWait(monitoringDataModel);
		}
		
		System.out.println("Conversion finished.");
		
		
		System.out.println("Storing MDM in "+OUTPUT_FILE);
		writeMdmFile(monitoringDataModel, OUTPUT_FILE);
		
		//decoding again and printing for debugging purposes
		readAndPrintMdmFile(OUTPUT_FILE);
		
		
		
	}


	private static void writeMdmFile(MonitoringDataSetImpl mdm, String path) {
		try {
			//create new, empty file
			File result = new File(path);
			if(result.exists()) {
				result.delete();
			}
			result.createNewFile();
						
			MDMSerializer serializer = new KryoMDMSerializer();
			
			serializer.prepare(new FileOutputStream(result));
			serializer.writeMonitoringDataSet(mdm);
			serializer.close();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}



	private static void readAndPrintMdmFile(String path) {
		File file = new File(path);
		MDMDeserializer deserializer = new KryoMDMDeserializer();
		
		DebugUtils util = new DebugUtils();
		
		try {
			
			deserializer.setSource(new FileInputStream(file));
			System.out.println("Reading from file "+path+"..");
			
			MonitoringDataSet mdm;
			while( (mdm = (MonitoringDataSet) deserializer.readNext()) != null) {
				System.out.println("MDM encoutnered:");
				mdm.getRootEvents().stream()
				.filter((e) -> e instanceof SessionAwareEvent)
				.limit(800)
				.forEachOrdered(util::printEvent);
				
			}
			
			System.out.println("EOF reached.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	
}
