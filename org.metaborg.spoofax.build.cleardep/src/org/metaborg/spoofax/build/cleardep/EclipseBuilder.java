package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.builders.All;
import org.metaborg.spoofax.build.cleardep.builders.Clean;
import org.strategoxt.imp.metatooling.JarsAntPropertyProvider;
import org.strategoxt.imp.metatooling.NativePrefixAntPropertyProvider;
import org.strategoxt.imp.metatooling.PluginClasspathProvider;
import org.strategoxt.imp.metatooling.StrategoJarAntPropertyProvider;
import org.strategoxt.imp.metatooling.StrategoMinJarAntPropertyProvider;
import org.sugarj.cleardep.BuildUnit;
import org.sugarj.cleardep.BuildUnit.ModuleVisitor;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.build.BuildRequest;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * updates editors to show newly built results
 * 
 * @author Sebastian Erdweg <seba at informatik uni-marburg de>
 */
public class EclipseBuilder extends IncrementalProjectBuilder {

	public static Properties makeSpoofaxProperties(Path baseDir) {
		Properties props = new Properties();
		props.put("trans", "trans");
		props.put("src-gen", "editor/java");
		props.put("syntax", "src-gen/syntax");
		props.put("include", "include");
		props.put("lib", "lib");
		props.put("build", "target/classes");
		props.put("dist", "bin/dist");
		props.put("pp", "src-gen/pp");
		props.put("signatures", "src-gen/signatures");
		props.put("completions", "src-gen/completions");
		props.put("sdf-src-gen", "src-gen");
		props.put("lib-gen", "include");

		props.put("eclipse.spoofaximp.nativeprefix", new NativePrefixAntPropertyProvider().getAntPropertyValue(null));
		props.put("eclipse.spoofaximp.strategojar", new StrategoJarAntPropertyProvider().getAntPropertyValue(null));
		props.put("eclipse.spoofaximp.strategominjar", new StrategoMinJarAntPropertyProvider().getAntPropertyValue(null));
		props.put("eclipse.spoofaximp.jars", new JarsAntPropertyProvider().getAntPropertyValue(null));
		props.put("externaljarx", new PluginClasspathProvider().getAntPropertyValue(null));
		
		String lang;
		Path[] sdfImports;
		RelativePath antBuildXML = new RelativePath(baseDir, "build.main.xml");
		try {
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(antBuildXML.getFile());
			Element project = doc.getDocumentElement();
			lang = project.getAttribute("name");
			
			Node kid = project.getFirstChild();
			sdfImports = null;
			while (kid != null) {
				if ("property".equals(kid.getNodeName()) && kid.hasAttributes()) {
					Node name = kid.getAttributes().getNamedItem("name");
					if (name != null && "build.sdf.imports".equals(name.getNodeValue())) {
						Node value = kid.getAttributes().getNamedItem("value");
						if (value != null) {
							String[] imports = value.getNodeValue().split("[\\s]*" + Pattern.quote("-Idef") + "[\\s]+");
							List<Path> paths = new ArrayList<>();
							for (String imp : imports)
								if (!imp.isEmpty()) {
									String subst = props.substitute(imp);
									if (AbsolutePath.acceptable(subst))
										paths.add(new AbsolutePath(subst));
									else
										paths.add(new RelativePath(baseDir, subst));
								}
							sdfImports = paths.toArray(new Path[paths.size()]);
							break;
						}
					}
				}
				kid = kid.getNextSibling();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		
		props.put("basedir", baseDir.getAbsolutePath());
		props.put("sdfmodule", lang);
		props.put("metasdfmodule", "Stratego-" + lang);
		props.put("esvmodule", lang);
		props.put("strmodule", lang.toLowerCase());
		props.put("ppmodule", lang + "-pp");
		props.put("sigmodule", lang + "-sig");
		
		if (sdfImports != null) {
			StringBuilder importString = new StringBuilder();
			for (Path imp : sdfImports)
				importString.append("-Idef " + props.substitute(imp.getAbsolutePath()));
			props.put("build.sdf.imports", importString.toString());
		}

		return props;
	}
	
	public static SpoofaxContext makeContext(IProject project) {
		Log.out = EclipseConsole.getOutputPrintStream();
	    Log.err = EclipseConsole.getErrorPrintStream();
	    Log.log.setLoggingLevel(Log.ALWAYS);
	    EclipseConsole.activateConsoleOnce();

	    Path baseDir = new AbsolutePath(project.getProject().getLocation().makeAbsolute().toString());
		Properties props = makeSpoofaxProperties(baseDir);
		return new SpoofaxContext(baseDir, props);
	}
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
		SpoofaxContext context = makeContext(getProject());
		SpoofaxInput input = new SpoofaxInput(context);

		try {
			BuildManager.build(new BuildRequest<>(All.factory, input));
			logFileStatistics(new BuildRequest<>(All.factory, input));
		} finally {
			monitor.done();
			try {
				getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void logFileStatistics(BuildRequest<?,?,?,?> req) {
		try {
			BuildUnit<?> result = BuildManager.readResult(req);
			
			final Set<Path> requiredFiles = new HashSet<>();
			final Set<Path> providedFiles = new HashSet<>();
			result.visit(new ModuleVisitor<Void>() {

				@Override
				public Void visit(BuildUnit<?> mod) {
					requiredFiles.addAll(mod.getExternalFileDependencies());
					providedFiles.addAll(mod.getGeneratedFiles());
					return null;
				}

				@Override
				public Void combine(Void t1, Void t2) {
					return null;
				}

				@Override
				public Void init() {
					return null;
				}

				@Override
				public boolean cancel(Void t) {
					return false;
				}
			});
			
			int allRequired = requiredFiles.size();
			requiredFiles.removeAll(providedFiles);
			int nongeneratedRequired = requiredFiles.size();
			int generatedRequired = allRequired - nongeneratedRequired;
			
			System.out.println("Generated " + providedFiles.size() + " files.");
			System.out.println("Required " + generatedRequired + " generated files.");
			System.out.println("Required " + nongeneratedRequired + " nongenerated files.");
		} catch (IOException e) {
		}		
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		SpoofaxContext context = makeContext(getProject());
		SpoofaxInput input = new SpoofaxInput(context);
		try {
			BuildManager.build(new BuildRequest<>(Clean.factory, input));
		} finally {
			monitor.done();
			try {
				getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
