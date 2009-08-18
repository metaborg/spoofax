package org.strategoxt.imp.metatooling.building;

import static org.eclipse.core.resources.IMarker.*;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.libstratego_lib;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorUpdater;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;

/**
 * Runs the project generator on modified editor descriptors.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorBuilder {
	
	// TODO: Use (and properly clean up) new marker type for internal errors?
	//       (also seen in DynamicDescriptorUpdater)
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
			if (resource.exists() && isMainFile(filename)) {
				monitor.beginTask("Building " + resource.getName(), IProgressMonitor.UNKNOWN);
				buildDescriptor(resource);
			}
			
		} catch (RuntimeException e) {
			Environment.logException("Unable to build descriptor for " + filename, e);
		} catch (Error e) { // workspace thread swallows this >:(
			Environment.logException("Unable to build descriptor for " + filename, e);
		}
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
			context.printStackTrace();
			return null;
		} finally {
			Debug.stopTimer("Invoked descriptor builder for " + mainFile.getName());
			libstratego_lib.dr_scope_all_end_0_0.instance.invoke(context, input);
		}
	}
	
	private static boolean isMainFile(String file) {
		// TODO: Determine if a file is the main descriptor file by its contents?
		// InputStream stream = agent.openInputStream(file);
		
		return file.matches(".*(-Main|\\.main)\\.esv");
	}
}
