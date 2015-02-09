package org.metaborg.spoofax.build.cleardep;

import org.sugarj.cleardep.build.BuildContext;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxBuildContext extends BuildContext {
	
	public final Path baseDir;
	public final Properties props;
	
	public SpoofaxBuildContext(Path baseDir, Properties props) {
		this.baseDir = baseDir;
		this.props = props;
	}
	
	public RelativePath relPath(String relative) { 
		return new RelativePath(baseDir, props.substitute(relative));
	}
}
