package org.metaborg.spoofax.build.cleardep.builders;


import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.build.cleardep.builders.aux.ParseSdfDefinition;
import org.metaborg.spoofax.build.cleardep.stampers.PPGenStamper;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_pp_pp_table_0_0;
import org.strategoxt.tools.main_ppgen_0_0;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.build.BuildRequest;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class PPGen extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, PPGen> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, PPGen>() {
		private static final long serialVersionUID = 9200673263670609032L;

		@Override
		public PPGen makeBuilder(SpoofaxInput context) { return new PPGen(context); }
	};
	
	public PPGen(SpoofaxInput context) {
		super(context);
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
	public None build() throws IOException {
		SpoofaxContext context = input.context;
		if (!context.isBuildStrategoEnabled(this))
			return None.val;
		
		BuildRequest<?,?,?,?> packSdf = new BuildRequest<>(PackSdf.factory, new PackSdf.Input(context));
		requireBuild(packSdf);

		RelativePath inputPath = context.basePath("${include}/${sdfmodule}.def");
		RelativePath ppOutputPath = context.basePath("${include}/${sdfmodule}.generated.pp");
		RelativePath afOutputPath = context.basePath("${include}/${sdfmodule}.generated.pp.af");
		
		if (SpoofaxContext.BETTER_STAMPERS) {
			BuildRequest<?, IStrategoTerm, ?, ?> parseSdfDefinition = new BuildRequest<>(ParseSdfDefinition.factory, new ParseSdfDefinition.Input(context, inputPath, new BuildRequest<?,?,?,?>[]{packSdf}));
			require(inputPath, new PPGenStamper(parseSdfDefinition));
		}
		else
			require(inputPath);
		
		ExecutionResult er1 = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_ppgen_0_0.instance, "main-ppgen", new LoggingFilteringIOAgent(Pattern.quote("[ main-ppgen | warning ]") + ".*"),
				"-i", inputPath,
				"-t",
				"-b",
				"-o", afOutputPath);
		generate(afOutputPath);
		
		// requires(afOutputPath);
		ExecutionResult er2 = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_pp_pp_table_0_0.instance, "main-pp-pp-table", new LoggingFilteringIOAgent(),
				"-i", afOutputPath,
				"-o", ppOutputPath);
		generate(ppOutputPath);
		
		if (!FileCommands.exists(afOutputPath)) {
			FileCommands.writeToFile(afOutputPath, "PP-Table([])");
			generate(afOutputPath);
		}
		
		setState(State.finished(er1.success && er2.success));
		
		return None.val;
	}

}
