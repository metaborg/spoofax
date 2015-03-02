package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CopyUtils extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, CopyUtils> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, CopyUtils>() {
		private static final long serialVersionUID = 2088788942202940759L;

		@Override
		public CopyUtils makeBuilder(SpoofaxInput input) { return new CopyUtils(input); }
	};
	
	public CopyUtils(SpoofaxInput input) {
		super(input);
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
	public None build() throws IOException {
		Path utils = context.basePath("utils");
		FileCommands.createDir(utils);
		
		String base = context.props.getOrFail("eclipse.spoofaximp.jars");
		for (String p : new String[]{"make_permissive.jar", "sdf2imp.jar", "aster.jar", "StrategoMix.def"}) {
			Path from = new AbsolutePath(base + "/" + p);
			Path to = new RelativePath(utils, p);
			requires(from);
			FileCommands.copyFile(from, to);
			generates(to);
		}
		
		Path strategojar = new AbsolutePath(context.props.getOrFail("eclipse.spoofaximp.strategojar"));
		Path strategojarTo = new RelativePath(utils, FileCommands.dropDirectory(strategojar));
		requires(strategojar);
		FileCommands.copyFile(strategojar, strategojarTo);
		generates(strategojarTo);
		
		return None.val;
	}
}
