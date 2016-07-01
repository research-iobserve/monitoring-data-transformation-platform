package org.mdtp.evaluation.jmeter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BroadleafManager {
	
	private Process runningTomcat;
	private String siteFolder;
	private String mavenPath;

	public BroadleafManager(String siteFolder, String mavenPath) {
		this.siteFolder = siteFolder;
		this.mavenPath = mavenPath;
	}
	
	public void start(String args) {

		System.out.println("Starting database...");
		runMvnCommand(siteFolder, "antrun:run@hsqldb-start", "");
		

		System.out.println("Starting tomcat...");
		runningTomcat = runMvnCommandAsync(siteFolder, "tomcat7:run-war", 
				"-Xmx1536M -XX:MaxPermSize=512M"
				+ " -javaagent:target/agents/spring-instrument.jar "+args);
		
		try {
			Thread.sleep( 2 * 60 * 1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Server is started.");
		
	}
	
	private void runMvnCommand(String workingDir, String mavenGoals, String opts) {
		List<String> cmd = new ArrayList<String>();
		cmd.add(mavenPath);
		cmd.addAll(Arrays.asList(mavenGoals.split(" ")));
		
		
		ProcessBuilder pb = new ProcessBuilder(cmd);
		
		pb.environment().put("MAVEN_OPTS", opts);
		
		pb.directory(new File(workingDir));
		pb.inheritIO();
		try {
			Process mvnRun = pb.start();
			mvnRun.waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Process runMvnCommandAsync(String workingDir, String mavenGoals, String opts) {
		List<String> cmd = new ArrayList<String>();
		cmd.add(mavenPath);
		cmd.addAll(Arrays.asList(mavenGoals.split(" ")));
		
		
		ProcessBuilder pb = new ProcessBuilder(cmd);
		
		pb.environment().put("MAVEN_OPTS", opts);
		
		pb.directory(new File(workingDir));
		pb.inheritIO();
		try {
			Process mvnRun = pb.start();
			return mvnRun;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void terminate() throws InterruptedException {
		System.out.println("killing tomcat..");
		try {
			killUnixProcess(runningTomcat);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		runningTomcat.waitFor();
		System.out.println("tomcat is dead.");
		System.out.println("Stopping database....");
		runMvnCommand(siteFolder, "antrun:run@hsqldb-stop", "");
		System.out.println("Database stopped.");
	}
	
	public static int getUnixPID(Process process) throws Exception
	{
	    System.out.println(process.getClass().getName());
	    if (process.getClass().getName().equals("java.lang.UNIXProcess"))
	    {
	        Class<?> cl = process.getClass();
	        Field field = cl.getDeclaredField("pid");
	        field.setAccessible(true);
	        Object pidObject = field.get(process);
	        return (Integer) pidObject;
	    } else
	    {
	        throw new IllegalArgumentException("Needs to be a UNIXProcess");
	    }
	}

	public static int killUnixProcess(Process process) throws Exception
	{
	    int pid = getUnixPID(process);
	    return Runtime.getRuntime().exec("kill " + pid).waitFor();
	}

}
