package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CopyUtils extends SpoofaxBuilder<SpoofaxInput> {

	public static SpoofaxBuilderFactory<SpoofaxInput, CopyUtils> factory = new SpoofaxBuilderFactory<SpoofaxInput, CopyUtils>() {
		private static final long serialVersionUID = 2088788942202940759L;

		@Override
		public CopyUtils makeBuilder(SpoofaxInput input, BuildManager manager) { return new CopyUtils(input, manager); }
	};
	
	public CopyUtils(SpoofaxInput input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Copy utilities";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("copyUtils.dep");
	}

	@Override
	public void build(CompilationUnit result) throws IOException {
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
