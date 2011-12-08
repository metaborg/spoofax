package org.strategoxt.imp.metatooling.building;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.interpreter.terms.IStrategoString;
import org.strategoxt.imp.editors.spoofax.generated.build_spoofaxlang_jvm_0_0;
import org.strategoxt.imp.editors.spoofax.generated.spoofaxlang;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoErrorExit;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;

/**
 * Triggers spoofaxlang building and loading from an Ant build file.
 */
public class AntSpxGenerateArtefacts {
	//TODO :  Set Derived Resources 
	//TODO :  Adding auto-generating the derived entries

	private static volatile boolean active;
	
	public static boolean isActive() {
		return active;
	}
	
	private static void refresh(IResource workingdir) throws Exception
	{
		final int depthArg =  IResource.DEPTH_INFINITE;
		final IResource file = workingdir;

		Job job = new Job("Refresh") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					file.touch(monitor);
					file.refreshLocal(depthArg, monitor);
				} catch (Exception e) {
					Environment.logWarning("Could not refresh file", e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(5000);
		
	}
	
	public static void main(String[] args) throws Exception{
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("Project Work Directory is missing. ");

		
		final String workingDirectoryArg = args[0];
		final IResource file = EditorIOAgent.getResource(new File(workingDirectoryArg));
		String buildStrategy = "-i"; 
		
		if (args.length >1)
			buildStrategy = args[1];
		
		Environment.getStrategoLock().lock();
		try {
			if (!isActive())
			{
				active = true;
				try {
					if (!file.exists()) {
						Environment.logException("Could not find project at following location :" + file.getLocation(), new FileNotFoundException(file.getFullPath().toOSString()));
						System.err.println("Build failed: could not find  project at following location :" + file.getLocation());
						System.exit(1);
					}

					EditorIOAgent agent = new EditorIOAgent();
					agent.setAlwaysActivateConsole(true);
					agent.setWorkingDir(file.getLocation().toOSString());

					boolean success = generateArtefacts(file, new NullProgressMonitor() , agent,buildStrategy);
					if (!success) {
						System.err.println("Build failed; see error log.");
						System.exit(1);
					}
				} finally {
					active = false;
				}
			}	
		} finally {
			Environment.getStrategoLock().unlock();
			refresh(file);
		}
	}

	
	public static boolean generateArtefacts(IResource file, IProgressMonitor monitor , EditorIOAgent agent,  String buildStrategy) {
		
		IPath location = file.getLocation();
		if (location == null) return false;
	
		try {
			monitor.setTaskName("Generating artefacts for following spx project:  " + file.getName());
			
			if (file.exists() ) {
				Context contextSpoofaxLang = new Context(Environment.getTermFactory(), agent);
				contextSpoofaxLang.registerClassLoader(spoofaxlang.class.getClassLoader());
				spoofaxlang.init(contextSpoofaxLang);
				
				IStrategoString input = contextSpoofaxLang.getFactory().makeString(file.getLocation().toOSString());
				IStrategoString buildStrategyTerm = contextSpoofaxLang.getFactory().makeString(buildStrategy);
				
				
				dr_scope_all_start_0_0.instance.invoke(contextSpoofaxLang, input);
				
				try {
					System.out.println("Compiling SPX files and generating intermediate artefacts.");
					System.out.println("Invoking build-spoofaxlang-jvm.");
				
					build_spoofaxlang_jvm_0_0.instance.invoke( contextSpoofaxLang , contextSpoofaxLang.getFactory().makeTuple(input, buildStrategyTerm));
					System.out.println("Intermediate artefacts have been generated successfully.");
					
				} catch (StrategoErrorExit e) {
					Environment.logException(e);
					throw new StrategoException("Project builder failed: " + e.getMessage() + "\nLog follows:\n\n"
							+ agent.getLog(), e);
				} catch (StrategoExit e) {
					if (e.getValue() != 0) {
						throw new StrategoException("Project builder failed.\nLog follows:\n\n"
								+ agent.getLog(), e);
					}
				}
				finally {
					dr_scope_all_end_0_0.instance.invoke(contextSpoofaxLang, input);
				}
				
				monitor.setTaskName(null);
			}
			return true;

		} catch (Exception e) {
			Environment.logException("Project builder failed: to generate artefacts" + file, e);
			return false;
		}

	}	
}
