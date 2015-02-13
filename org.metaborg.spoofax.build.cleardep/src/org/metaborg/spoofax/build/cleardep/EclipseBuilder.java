package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.strategoxt.HybridInterpreter;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

/**
 * updates editors to show newly built results
 * 
 * @author Sebastian Erdweg <seba at informatik uni-marburg de>
 */
public class EclipseBuilder extends IncrementalProjectBuilder {
	
	public static SpoofaxBuildContext makeContext(IProject project) {
		Log.out = EclipseConsole.getOutputPrintStream();
	    Log.err = EclipseConsole.getErrorPrintStream();
	    Log.log.setLoggingLevel(Log.ALWAYS);
	    EclipseConsole.activateConsoleOnce();

		Path baseDir = new AbsolutePath(project.getProject().getLocation().makeAbsolute().toString());
		Path binDir = new RelativePath(baseDir, "include");

		// FIXME use actual Spoofax language name
		Properties props = Properties.makeSpoofaxProperties("TemplateLang", new Path[] {new RelativePath(baseDir, "${lib}/SDF.def")});
		
		SpoofaxBuildContext context = new SpoofaxBuildContext(baseDir, binDir, props, new HybridInterpreter());
		return context;
	}
	
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
		SpoofaxBuildContext context = makeContext(getProject());
		try {
			context.all.require(null, new SimpleMode());
			getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			monitor.done();
		}
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		SpoofaxBuildContext context = makeContext(getProject());
		try {
			context.clean.require(null, new SimpleMode());
			getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			monitor.done();
		}
	}
}
