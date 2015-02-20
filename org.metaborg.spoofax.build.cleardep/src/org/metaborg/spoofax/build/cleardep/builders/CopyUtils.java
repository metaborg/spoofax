package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CopyUtils extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, CopyUtils> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, CopyUtils>() {
		@Override
		public CopyUtils makeBuilder(SpoofaxBuildContext context) { return new CopyUtils(context); }
	};
	
	public CopyUtils(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Void input) {
		return "Copy utilities";
	}
	
	@Override
	public Path persistentPath(Void input) {
		return context.depPath("copyUtils.dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		Path utils = context.basePath("utils");
		FileCommands.createDir(utils);
		
		String base = context.props.getOrFail("eclipse.spoofaximp.jars");
		for (String p : new String[]{"make_permissive.jar", "sdf2imp.jar", "aster.jar", "StrategoMix.def"}) {
			Path from = new AbsolutePath(base + "/" + p);
			Path to = new RelativePath(utils, p);
			result.addExternalFileDependency(from);
			FileCommands.copyFile(from, to);
			result.addGeneratedFile(to);
		}
		
		Path strategojar = new AbsolutePath(context.props.getOrFail("eclipse.spoofaximp.strategojar"));
		Path strategojarTo = new RelativePath(utils, FileCommands.dropDirectory(strategojar));
		result.addExternalFileDependency(strategojar);
		FileCommands.copyFile(strategojar, strategojarTo);
		result.addGeneratedFile(strategojarTo);
	}
}
