package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.builders.All;
import org.metaborg.spoofax.build.cleardep.builders.Clean;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;

/**
 * updates editors to show newly built results
 * 
 * @author Sebastian Erdweg <seba at informatik uni-marburg de>
 */
public class EclipseBuilder extends IncrementalProjectBuilder {
	
	public static SpoofaxContext makeContext(IProject project) {
		Log.out = EclipseConsole.getOutputPrintStream();
	    Log.err = EclipseConsole.getErrorPrintStream();
	    Log.log.setLoggingLevel(Log.ALWAYS);
	    EclipseConsole.activateConsoleOnce();

		Path baseDir = new AbsolutePath(project.getProject().getLocation().makeAbsolute().toString());
		return new SpoofaxContext(baseDir, new Properties());
	}
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
		BuildManager manager = new BuildManager();
		SpoofaxContext context = makeContext(getProject());
		SpoofaxInput input = new SpoofaxInput(context);
		try {
			
			manager.require(All.factory.makeBuilder(input, manager), new SimpleMode());
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

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		BuildManager manager = new BuildManager();
		SpoofaxContext context = makeContext(getProject());
		SpoofaxInput input = new SpoofaxInput(context);
		try {
			manager.require(Clean.factory.makeBuilder(input, manager), new SimpleMode());
			getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			monitor.done();
		}
	}
}
