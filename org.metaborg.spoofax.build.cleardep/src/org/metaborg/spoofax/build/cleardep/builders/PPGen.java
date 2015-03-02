package org.metaborg.spoofax.build.cleardep.builders;


import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.tools.main_pp_pp_table_0_0;
import org.strategoxt.tools.main_ppgen_0_0;
import org.sugarj.cleardep.BuildUnit;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class PPGen extends SpoofaxBuilder<SpoofaxInput> {

	public static SpoofaxBuilderFactory<SpoofaxInput, PPGen> factory = new SpoofaxBuilderFactory<SpoofaxInput, PPGen>() {
		private static final long serialVersionUID = 9200673263670609032L;

		@Override
		public PPGen makeBuilder(SpoofaxInput context, BuildManager manager) { return new PPGen(context, manager); }
	};
	
	public PPGen(SpoofaxInput context, BuildManager manager) {
		super(context, factory, manager);
	}
	
	@Override
	protected String taskDescription() {
		return "Generate pretty-print table from grammar";
	}
	
	@Override
	protected Path persistentPath() {
		return input.context.depPath("ppGen.dep");
	}

	@Override
	public void build(BuildUnit result) throws IOException {
		SpoofaxContext context = input.context;
		if (!context.isBuildStrategoEnabled(result))
			return;
		
		require(PackSdf.factory, new PackSdf.Input(context));

		RelativePath inputPath = context.basePath("${include}/${sdfmodule}.def");
		RelativePath ppOutputPath = context.basePath("${include}/${sdfmodule}.generated.pp");
		RelativePath afOutputPath = context.basePath("${include}/${sdfmodule}.generated.pp.af");
		
		result.requires(inputPath);
		ExecutionResult er1 = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_ppgen_0_0.instance, "main-ppgen", new LoggingFilteringIOAgent(Pattern.quote("[ main-ppgen | warning ]") + ".*"),
				"-i", inputPath,
				"-t",
				"-b",
				"-o", afOutputPath);
		result.generates(afOutputPath);
		
		result.requires(afOutputPath);
		ExecutionResult er2 = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_pp_pp_table_0_0.instance, "main-pp-pp-table", new LoggingFilteringIOAgent(),
				"-i", afOutputPath,
				"-o", ppOutputPath);
		result.generates(ppOutputPath);
		
		if (!FileCommands.exists(afOutputPath)) {
			FileCommands.writeToFile(afOutputPath, "PP-Table([])");
			result.generates(afOutputPath);
		}
		
		result.setState(State.finished(er1.success && er2.success));
	}

}
