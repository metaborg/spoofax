package org.metaborg.spoofax.build.cleardep;

import org.metaborg.spoofax.build.cleardep.builders.All;
import org.metaborg.spoofax.build.cleardep.builders.Clean;
import org.metaborg.spoofax.build.cleardep.builders.ForceOnSave;
import org.metaborg.spoofax.build.cleardep.builders.MakePermissive;
import org.metaborg.spoofax.build.cleardep.builders.PPPack;
import org.metaborg.spoofax.build.cleardep.builders.PackSdf;
import org.metaborg.spoofax.build.cleardep.builders.Sdf2Table;
import org.metaborg.spoofax.build.cleardep.builders.SpoofaxDefaultCtree;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.lang.Context;
import org.sugarj.cleardep.build.BuildContext;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxBuildContext extends BuildContext {
	
	
	public Clean clean = Clean.factory.makeBuilder(this);
	public All all = All.factory.makeBuilder(this);
	public PPPack ppPack = PPPack.factory.makeBuilder(this);
	public SpoofaxDefaultCtree spoofaxDefaultCtree = SpoofaxDefaultCtree.factory.makeBuilder(this);
	public ForceOnSave forceOnSave = ForceOnSave.factory.makeBuilder(this);
	public PackSdf packSdf = PackSdf.factory.makeBuilder(this);
	public MakePermissive makePermissive = MakePermissive.factory.makeBuilder(this);
	public Sdf2Table sdf2Table = Sdf2Table.factory.makeBuilder(this);
	
	public final Path baseDir;
	public final Path binDir;
	public final Properties props;
	public final HybridInterpreter interp;
	
	private static Context toolsContext;
	private static Context permissiveGrammarsContext;
	private static Context xtcContext;
	
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
	
	public Context toolsContext() {
		synchronized (SpoofaxBuildContext.class) {
			if (toolsContext != null)
				return toolsContext;
			toolsContext = org.strategoxt.tools.tools.init();
			return toolsContext;
		}
	}
	
	public Context permissiveGrammarsContext() {
		synchronized (SpoofaxBuildContext.class) {
			if (permissiveGrammarsContext != null)
				return permissiveGrammarsContext;
			permissiveGrammarsContext = org.strategoxt.permissivegrammars.permissivegrammars.init();
			return permissiveGrammarsContext;
		}
	}

	public Context xtcContext() {
		synchronized (SpoofaxBuildContext.class) {
			if (xtcContext != null)
				return xtcContext;
			xtcContext = org.strategoxt.stratego_xtc.stratego_xtc.init();
			return xtcContext;
		}
	}
}
