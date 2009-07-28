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
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.libstratego_lib;
import org.strategoxt.imp.editors.editorservice.EditorServiceParseController;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorUpdater;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;

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
	
	// TODO: Use (and properly clean up) new marker type for internal errors?
	private final AstMessageHandler messageHandler =
		new AstMessageHandler(AstMessageHandler.ANALYSIS_MARKER_TYPE);
	
	private final Context context;
	
	private final FileTrackingIOAgent agent;
	
	private final DynamicDescriptorUpdater loader;
	
	public DynamicDescriptorBuilder(DynamicDescriptorUpdater loader) {
		try {
			this.loader = loader;
			
			agent = new FileTrackingIOAgent();		
			agent.setDescriptor(EditorServiceParseController.getDescriptor());
			context = new Context(Environment.getTermFactory());
			context.setIOAgent(agent);
			sdf2imp.init(context);
			
		} catch (Throwable e) { // (catch classes not loading, etc.)
			Environment.logException("Unable to initialize dynamic builder", e);
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
			} else if (!isMainFile(filename)) {
				// Simply ignore non main files for now
				// Environment.logException("Could not resolve dependencies for updated file " + resource.getName());
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
			IStrategoTerm result = invokeBuilder(mainFile);
			if (result == null) {
				String log = agent.getLog().trim();
				messageHandler.addMarkerFirstLine(mainFile,
						"Unable to build descriptor: \n" + log, SEVERITY_ERROR);
				return;
			}
			
			updateDependencies(mainFile);
			
			String resultPath = ((IStrategoString) result).stringValue();
			IResource packedDescriptor = mainFile.getParent().getFile(Path.fromOSString(resultPath));
			loader.loadPackedDescriptor(packedDescriptor);
			
			// XXX: The generated file should be refreshed after rebuilding
			
		} catch (IOException e) {
			Environment.logException("Unable to build descriptor for " + mainFile, e);
			messageHandler.addMarkerFirstLine(mainFile, "Internal error building descriptor:" + e, SEVERITY_ERROR);
		}
	}

	/**
	 * Invoke the Stratego-based descriptor builder.
	 * 
	 * @return  <code>true</code> if successful.
	 */
	private IStrategoTerm invokeBuilder(IResource mainFile) throws IOException {
		IPath location = mainFile.getRawLocation();
		String path = location.removeLastSegments(1).toOSString();
		String filename = mainFile.getName();
		IStrategoString input = context.getFactory().makeString(filename);
		
		agent.clearLog();
		agent.setWorkingDir(path);
		libstratego_lib.dr_scope_all_start_0_0.instance.invoke(context, input);
		
		try {
			Debug.startTimer();
			return sdf2imp.sdf2imp_jvm_0_0.instance.invoke(context, input);
		} catch (StrategoExit e) {
			Environment.logException("Unexpected exit in dynamic builder", e);
			return null;
		} finally {
			Debug.stopTimer("Invoked descriptor builder for " + mainFile.getName());
			libstratego_lib.dr_scope_all_end_0_0.instance.invoke(context, input);
		}
	}

	private void updateDependencies(IResource mainFile) {
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
		// InputStream stream = agent.openInputStream(file);
		
		return file.matches(".*(-Main|\\.main)\\.esv");
	}
}
