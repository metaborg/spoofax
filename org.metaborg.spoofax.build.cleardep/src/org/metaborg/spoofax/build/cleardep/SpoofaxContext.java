package org.metaborg.spoofax.build.cleardep;

import org.strategoxt.lang.Context;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxContext {
	
	public final Path baseDir;
	public final Properties props;
	
	private static Context toolsContext;
	private static Context permissiveGrammarsContext;
	private static Context xtcContext;
	private static Context generatorContext;
	
	public SpoofaxContext(Path baseDir, Properties props) {
		this.baseDir = baseDir;
		this.props = props;
	}
	
	public RelativePath basePath(String relative) { 
		return new RelativePath(baseDir, props.substitute(relative));
	}
	
	public RelativePath depDir() { 
		return new RelativePath(baseDir, props.substitute("${include}/build"));
	}
	
	public RelativePath depPath(String relative) { 
		return new RelativePath(baseDir, props.substitute("${include}/build/" + relative));
	}
	
	public Context toolsContext() {
		synchronized (SpoofaxContext.class) {
			if (toolsContext != null)
				return toolsContext;
			toolsContext = org.strategoxt.tools.tools.init();
			return toolsContext;
		}
	}
	
	public Context permissiveGrammarsContext() {
		synchronized (SpoofaxContext.class) {
			if (permissiveGrammarsContext != null)
				return permissiveGrammarsContext;
			permissiveGrammarsContext = org.strategoxt.permissivegrammars.permissivegrammars.init();
			return permissiveGrammarsContext;
		}
	}

	public Context xtcContext() {
		synchronized (SpoofaxContext.class) {
			if (xtcContext != null)
				return xtcContext;
			xtcContext = org.strategoxt.stratego_xtc.stratego_xtc.init();
			return xtcContext;
		}
	}
	
	public Context generatorContext() {
		synchronized (SpoofaxContext.class) {
			if (generatorContext != null)
				return generatorContext;
			generatorContext = org.strategoxt.imp.generator.generator.init();
			return generatorContext;
		}
	}
	
	public Context strjContext() {
	    // strj requires a fresh context each time.
		return org.strategoxt.strj.strj.init();
	}
	
	public boolean isBuildStrategoEnabled(CompilationUnit result) {
		RelativePath strategoPath = basePath("${trans}/${strmodule}.str");
		result.addExternalFileDependency(strategoPath);
		boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
		return buildStrategoEnabled;
	}
	
	public boolean isJavaJarEnabled(CompilationUnit result) {
		RelativePath mainPath = basePath("${src-gen}/org/strategoxt/imp/editors/template/strategies/Main.java");
		result.addExternalFileDependency(mainPath);
		return FileCommands.exists(mainPath);
	}
}
