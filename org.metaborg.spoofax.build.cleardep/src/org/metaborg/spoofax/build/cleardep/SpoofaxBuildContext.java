package org.metaborg.spoofax.build.cleardep;

import org.metaborg.spoofax.build.cleardep.builders.All;
import org.metaborg.spoofax.build.cleardep.builders.Clean;
import org.metaborg.spoofax.build.cleardep.builders.CopySdf;
import org.metaborg.spoofax.build.cleardep.builders.ForceOnSave;
import org.metaborg.spoofax.build.cleardep.builders.ForceOnSaveFile;
import org.metaborg.spoofax.build.cleardep.builders.MakePermissive;
import org.metaborg.spoofax.build.cleardep.builders.MetaSdf2Table;
import org.metaborg.spoofax.build.cleardep.builders.PPGen;
import org.metaborg.spoofax.build.cleardep.builders.PPPack;
import org.metaborg.spoofax.build.cleardep.builders.PackSdf;
import org.metaborg.spoofax.build.cleardep.builders.Sdf2ImpEclipse;
import org.metaborg.spoofax.build.cleardep.builders.Sdf2Parenthesize;
import org.metaborg.spoofax.build.cleardep.builders.Sdf2Rtg;
import org.metaborg.spoofax.build.cleardep.builders.Sdf2Table;
import org.metaborg.spoofax.build.cleardep.builders.SpoofaxDefaultCtree;
import org.metaborg.spoofax.build.cleardep.builders.StrategoAster;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.lang.Context;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.build.BuildContext;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxBuildContext extends BuildContext {
	
	
	public Clean clean = Clean.factory.makeBuilder(this);
	public All all = All.factory.makeBuilder(this);
	public PPPack ppPack = PPPack.factory.makeBuilder(this);
	public SpoofaxDefaultCtree spoofaxDefaultCtree = SpoofaxDefaultCtree.factory.makeBuilder(this);
	public ForceOnSave forceOnSave = ForceOnSave.factory.makeBuilder(this);
	public ForceOnSaveFile forceOnSaveFile = ForceOnSaveFile.factory.makeBuilder(this);
	public PackSdf packSdf = PackSdf.factory.makeBuilder(this);
	public CopySdf copySdf = CopySdf.factory.makeBuilder(this);
	public MakePermissive makePermissive = MakePermissive.factory.makeBuilder(this);
	public Sdf2Table sdf2Table = Sdf2Table.factory.makeBuilder(this);
	public MetaSdf2Table metaSdf2Table = MetaSdf2Table.factory.makeBuilder(this);
	public PPGen ppGen = PPGen.factory.makeBuilder(this);
	public Sdf2Rtg sdf2Rtg = Sdf2Rtg.factory.makeBuilder(this);
	public Sdf2ImpEclipse sdf2ImpEclipse = Sdf2ImpEclipse.factory.makeBuilder(this);
	public Sdf2Parenthesize sdf2Parenthesize = Sdf2Parenthesize.factory.makeBuilder(this);
	public StrategoAster strategoAster = StrategoAster.factory.makeBuilder(this);
	
	public final Path baseDir;
	public final Properties props;
	public final HybridInterpreter interp;
	
	private static Context toolsContext;
	private static Context permissiveGrammarsContext;
	private static Context xtcContext;
	private static Context generatorContext;
	
	public SpoofaxBuildContext(Path baseDir, Properties props, HybridInterpreter interp) {
		super(new BuildManager());
		this.baseDir = baseDir;
		this.props = props;
		this.interp = interp;
	}
	
	public RelativePath basePath(String relative) { 
		return new RelativePath(baseDir, props.substitute(relative));
	}
	
	public RelativePath depPath(String relative) { 
		return new RelativePath(baseDir, props.substitute("${include}/build/" + relative));
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
	
	public Context generatorContext() {
		synchronized (SpoofaxBuildContext.class) {
			if (generatorContext != null)
				return generatorContext;
			generatorContext = org.strategoxt.imp.generator.generator.init();
			return generatorContext;
		}
	}
	
	public boolean isBuildStrategoEnabled(CompilationUnit result) {
		RelativePath strategoPath = basePath("${trans}/${strmodule}.str");
		result.addExternalFileDependency(strategoPath);
		boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
		return buildStrategoEnabled;
	}
}
