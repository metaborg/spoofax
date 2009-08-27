package org.strategoxt.imp.metatooling.building;

import static org.eclipse.core.resources.IMarker.*;
import static org.strategoxt.imp.metatooling.loading.DynamicDescriptorUpdater.*;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorUpdater;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.compat.SSL_EXT_java_call;
import org.strategoxt.libstratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.libstratego_lib.dr_scope_all_start_0_0;
import org.strategoxt.permissivegrammars.make_permissive;

/**
 * Runs the project generator on modified editor descriptors.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorBuilder {
	
	private final AstMessageHandler messageHandler =
		new AstMessageHandler(AstMessageHandler.ANALYSIS_MARKER_TYPE);
	
	private final Context context;
	
	private final EditorIOAgent agent;
	
	private final DynamicDescriptorUpdater loader;
	
	public DynamicDescriptorBuilder(DynamicDescriptorUpdater loader) {
		try {
			this.loader = loader;
			
			agent = new EditorIOAgent();
			context = new Context(Environment.getTermFactory(), agent);
			SSL_EXT_java_call.registerClassLoader(make_permissive.class.getClassLoader());
			sdf2imp.init(context);
			
		} catch (Throwable e) { // (catch classes not loading, etc.)
			Environment.logException("Unable to initialize dynamic builder", e);
			throw new RuntimeException("Unable to initialize dynamic builder", e);
		}
	}
	
	public void updateResource(IResource resource, IProgressMonitor monitor) {
		IPath location = resource.getRawLocation();
		if (location == null) return;
		
		try {
			if (resource.exists() && isMainFile(resource)) {
				monitor.beginTask("Building " + resource.getName(), IProgressMonitor.UNKNOWN);
				buildDescriptor(resource, monitor);
			}
			
		} catch (RuntimeException e) {
			Environment.logException("Unable to build descriptor for " + resource, e);
		} catch (Error e) { // workspace thread swallows this >:(
			Environment.logException("Unable to build descriptor for " + resource, e);
		}
	}

	/**
	 * Build and load a descriptor file.
	 */
	private void buildDescriptor(IResource mainFile, IProgressMonitor monitor) {
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
			
			monitor.beginTask("Loading " + mainFile.getName(), IProgressMonitor.UNKNOWN);
			loader.loadPackedDescriptor(getTargetDescriptor(mainFile));
			
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
		dr_scope_all_start_0_0.instance.invoke(context, input);
		
		try {
			Debug.startTimer();
			return sdf2imp.sdf2imp_jvm_0_0.instance.invoke(context, input);
		} catch (StrategoExit e) {
			Environment.logException("Unexpected exit in dynamic builder", e);
			context.printStackTrace();
			messageHandler.addMarkerFirstLine(mainFile, "Error building descriptor:" + e + "\n" + agent.getLog(), SEVERITY_ERROR);
			return null;
		} finally {
			Debug.stopTimer("Invoked descriptor builder for " + mainFile.getName());
			dr_scope_all_end_0_0.instance.invoke(context, input);
		}
	}
	
	private static boolean isMainFile(IResource file) {
		// TODO: Determine if a file is the main descriptor file by its contents?
		// InputStream stream = agent.openInputStream(file);
		
		if (file.getParent() != null && "bin".equals(file.getParent().getName()))
			return false;
		
		return file.toString().matches(".*(-Main|\\.main)\\.esv");
	}
}
