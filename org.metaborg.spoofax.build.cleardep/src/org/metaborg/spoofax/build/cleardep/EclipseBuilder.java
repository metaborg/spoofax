package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.build.cleardep.builders.All;
import org.metaborg.spoofax.build.cleardep.builders.Clean;
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
	
	public static Properties makeProperties(String lang) {
		Properties props = new Properties(new HashMap<String, String>());

		props.put("sdfmodule", lang);
		props.put("metasdfmodule", "Stratego-" + lang);
		props.put("esvmodule", lang);
		props.put("strmodule", lang.substring(0, 1).toLowerCase() + lang.substring(1));
		props.put("ppmodule", lang + "-pp");
		props.put("sigmodule", lang + "-sig");

		props.put("trans", "trans");
		props.put("trans.rel", "trans");
		props.put("src-gen", "editor/java");
		props.put("syntax", "src-gen/syntax");
		props.put("syntax.rel", props.get("syntax"));
		props.put("include", "include");
		props.put("include.rel", props.get("include"));
		props.put("lib", "lib");
		props.put("build", "target/classes");
		props.put("dist", "bin/dist");
		props.put("pp", "src-gen/pp");
		props.put("signatures", "src-gen/signatures");
		props.put("sdf-src-gen", "src-gen");
		props.put("lib-gen", "include");
		props.put("lib-gen.rel", props.get("lib-gen"));

		return props;
	}
	
	public static SpoofaxBuildContext makeContext(IProject project) {
		Log.out = EclipseConsole.getOutputPrintStream();
	    Log.err = EclipseConsole.getErrorPrintStream();
	    EclipseConsole.activateConsoleOnce();

		Path baseDir = new AbsolutePath(project.getProject().getLocation().makeAbsolute().toString());
		Path binDir = new RelativePath(baseDir, "include");
		// FIXME use actual Spoofax language name
		Properties props = makeProperties("TemplateLang");
		SpoofaxBuildContext context = new SpoofaxBuildContext(baseDir, binDir, props, new HybridInterpreter());
		return context;
	}
	
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
		SpoofaxBuildContext context = makeContext(getProject());
		try {
			All.factory.makeBuilder(context).require(null, context.basePath("${include}/build.all.dep"), new SimpleMode());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			monitor.done();
		}
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		SpoofaxBuildContext context = makeContext(getProject());
		try {
			Clean.factory.makeBuilder(context).require(null, context.basePath("${include}/build.clean.dep"), new SimpleMode());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			monitor.done();
		}
	}
}
