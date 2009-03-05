package org.strategoxt.imp.metatooling.building;

import static org.eclipse.core.resources.IMarker.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.library.LoggingIOAgent;
import org.spoofax.interpreter.terms.IStrategoString;
import org.strategoxt.imp.editors.editorservice.EditorServiceParseController;
import org.strategoxt.imp.metatooling.MetatoolingActivator;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorUpdater;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorBuilder {
	
	private final Map<String, Set<IResource>> mainEditorFiles =
		new HashMap<String, Set<IResource>>();
	
	private final Map<IResource, Set<String>> includedEditorFiles =
		new HashMap<IResource, Set<String>>();
	
	/**
	 * The set of main files that just has been updated in the current updating pass.
	 */
	private final Set<IResource> upToDateMainFiles =
		new HashSet<IResource>();
	
	private final AstMessageHandler messageHandler =
		new AstMessageHandler();
	
	private final Interpreter builder;
	
	private final DynamicDescriptorUpdater loader;
	
	public DynamicDescriptorBuilder(DynamicDescriptorUpdater loader) {
		try {
			this.loader = loader;
			
			Debug.startTimer("Loading dynamic editor builder");
			
			EditorIOAgent agent = new FileTrackingIOAgent();		
			agent.setDescriptor(EditorServiceParseController.getDescriptor());
			builder = Environment.createInterpreter();
			builder.setIOAgent(agent);
			Environment.addToInterpreter(builder, MetatoolingActivator.getResourceAsStream("/include/sdf2imp.ctree"));
			
			Debug.stopTimer("Successfully loaded dynamic editor builder");
			
		} catch (Throwable e) {
			throw new RuntimeException("Unable to initialize dynamic builder", e);
		}
	}
	
	public void updateResource(IResource resource, IProgressMonitor monitor) {
		IPath location = resource.getRawLocation();
		if (location == null) return;
		String filename = location.toOSString();
		
		try {
			Set<IResource> mainFiles = mainEditorFiles.get(filename);
			if (mainFiles != null) {
				for (IResource mainFile : mainFiles) {
					if (!upToDateMainFiles.contains(mainFile)) {
						upToDateMainFiles.add(mainFile);
						monitor.beginTask("Building " + mainFile.getName(), IProgressMonitor.UNKNOWN);
						buildDescriptor(mainFile);
					}
				}
			}
			
			if (isMainFile(filename) && !upToDateMainFiles.contains(resource)) {
				upToDateMainFiles.add(resource);
				monitor.beginTask("Building " + resource.getName(), IProgressMonitor.UNKNOWN);
				buildDescriptor(resource);
			}
			
		} catch (RuntimeException e) {
			Environment.logException("Unable to build descriptor for " + filename, e);
		} catch (Error e) { // workspace thread swallows this >:(
			Environment.logException("Unable to build descriptor for " + filename, e);
		}
	}
	
	public void invalidateUpdatedResources() {
		upToDateMainFiles.clear();
	}

	/**
	 * Build and load a descriptor file.
	 */
	private void buildDescriptor(IResource mainFile) {
		try {
			Environment.assertLock();
			messageHandler.clearMarkers(mainFile);
			boolean success = invokeBuilder(mainFile);
		
			if (!success) {
				String log = ((LoggingIOAgent) builder.getIOAgent()).getLog().trim();
				messageHandler.addMarkerFirstLine(mainFile,
						"Unable to build descriptor: \n" + log, SEVERITY_ERROR);
				return;
			}
			
			updateDependencies(mainFile);
		} catch (InterpreterException e) {
			Environment.logException("Unable to build descriptor for " + mainFile, e);
			messageHandler.addMarkerFirstLine(mainFile, "Internal error building descriptor:" + e, SEVERITY_ERROR);
		} catch (IOException e) {
			Environment.logException("Unable to build descriptor for " + mainFile, e);
			messageHandler.addMarkerFirstLine(mainFile, "Internal error building descriptor:" + e, SEVERITY_ERROR);
		}
		
		String result = ((IStrategoString) builder.current()).stringValue();
		IResource packedDescriptor = mainFile.getParent().getFile(Path.fromOSString(result));
		loader.loadPackedDescriptor(packedDescriptor);
	}

	/**
	 * Invoke the Stratego-based descriptor builder.
	 * 
	 * @return  <code>true</code> if successful.
	 */
	private boolean invokeBuilder(IResource mainFile)
			throws IOException, InterpreterException {
		
		((LoggingIOAgent) builder.getIOAgent()).clearLog();
		
		IPath location = mainFile.getRawLocation();
		String path = location.removeLastSegments(1).toOSString();
		String filename = mainFile.getName();
		
		builder.getIOAgent().setWorkingDir(path);
		builder.setCurrent(builder.getFactory().makeString(filename));
		builder.invoke("dr-scope-all-start");
		
		boolean success;
		
		try {
			Debug.startTimer();
			success = builder.invoke("sdf2imp-jvm");
		} catch (InterpreterExit e) {
			success = e.getValue() == InterpreterExit.SUCCESS;
		} finally {
			Debug.stopTimer("Invoked descriptor builder for " + mainFile.getName());
			builder.invoke("dr-scope-all-end");
		}
		
		return success;
	}

	private void updateDependencies(IResource mainFile) {
		FileTrackingIOAgent agent = (FileTrackingIOAgent) builder.getIOAgent();
		
		// Remove old dependencies		
		for (String oldDependency : includedEditorFiles.get(mainFile)) {
			Set<IResource> set = mainEditorFiles.get(oldDependency);
			if (set != null) set.remove(mainFile);
		}
		
		// Add new dependencies
		includedEditorFiles.put(mainFile, agent.getTracked());		
		for (String newDependency : agent.getTracked()) {
			newDependency = new Path(newDependency).toPortableString();
			Set<IResource> set = mainEditorFiles.get(newDependency);
			if (set == null) {
				set = new HashSet<IResource>();
				set.add(mainFile);
				mainEditorFiles.put(newDependency, set);
			} else {
				set.add(mainFile);
			}
		}
		
		agent.setTracked(new HashSet<String>());
	}
	
	private static boolean isMainFile(String file) {
		// TODO: Determine if a file is the main descriptor file by its contents?
		// InputStream stream = builder.getIOAgent().openInputStream(file);
		
		return file.matches(".*(-Main|\\.main)\\.esv");
	}
}
