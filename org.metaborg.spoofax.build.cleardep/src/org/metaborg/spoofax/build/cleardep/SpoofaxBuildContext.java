package org.metaborg.spoofax.build.cleardep;

import org.strategoxt.HybridInterpreter;
import org.strategoxt.lang.Context;
import org.sugarj.cleardep.build.BuildContext;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxBuildContext extends BuildContext {
	
	public final Path baseDir;
	public final Path binDir;
	public final Properties props;
	public final HybridInterpreter interp;
	
	public SpoofaxBuildContext(Path baseDir, Path binDir, Properties props, HybridInterpreter interp) {
		this.baseDir = baseDir;
		this.binDir = binDir;
		this.props = props;
		this.interp = interp;
	}
	
	public RelativePath basePath(String relative) { 
		return new RelativePath(baseDir, props.substitute(relative));
	}
	
	public RelativePath binPath(String relative) { 
		return new RelativePath(binDir, props.substitute(relative));
	}
	
	public Context strategoContext() {
		return interp.getCompiledContext();
	}
}
