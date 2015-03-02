package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.BuildUnit;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class StrategoAster extends SpoofaxBuilder<StrategoAster.Input> {

	public static SpoofaxBuilderFactory<Input, StrategoAster> factory = new SpoofaxBuilderFactory<Input, StrategoAster>() {
		private static final long serialVersionUID = -1290903435504555665L;

		@Override
		public StrategoAster makeBuilder(Input input, BuildManager manager) { return new StrategoAster(input, manager); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -4593910056510380042L;
		public final String strmodule;
		public Input(SpoofaxContext context, String strmodule) {
			super(context);
			this.strmodule = strmodule;
		}
	}
	
	public StrategoAster(Input input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Compile attribute grammar to Stratego";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("strategoAster." + input.strmodule + ".dep");
	}

	@Override
	public void build(BuildUnit result) throws IOException {
		List<RelativePath> asterInputList = FileCommands.listFilesRecursive(context.baseDir, new FileExtensionFilter("astr"));
		for (RelativePath p : asterInputList)
			result.requires(p);
//		String asterInput = StringCommands.printListSeparated(asterInputList, " ");
//		RelativePath outputPath = context.basePath("${trans}/" + input.strmodule + ".rtree");
		
		// TODO Aster compiler not available
//		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.asterContext, 
//				org.strategoxt.aster.Main.instance, "aster", new LoggingFilteringIOAgent(), 
//				"-i", asterInput);

//		result.addGeneratedFile(outputPath);
//		result.setState(State.finished(er.success));
	}
}
