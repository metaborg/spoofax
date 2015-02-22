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
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class PPGen extends SpoofaxBuilder<SpoofaxInput> {

	public static SpoofaxBuilderFactory<SpoofaxInput, PPGen> factory = new SpoofaxBuilderFactory<SpoofaxInput, PPGen>() {
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
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		SpoofaxContext context = input.context;
		if (!context.isBuildStrategoEnabled(result))
			return;
		
		require(PackSdf.factory, new PackSdf.Input(context), new SimpleMode());

		RelativePath inputPath = context.basePath("${include}/${sdfmodule}.def");
		RelativePath ppOutputPath = context.basePath("${include}/${sdfmodule}.generated.pp");
		RelativePath afOutputPath = context.basePath("${include}/${sdfmodule}.generated.pp.af");
		
		result.addSourceArtifact(inputPath);
		ExecutionResult er1 = StrategoExecutor.runStrategoCLI(context.toolsContext(), 
				main_ppgen_0_0.instance, "main-ppgen", new LoggingFilteringIOAgent(Pattern.quote("[ main-ppgen | warning ]") + ".*"),
				"-i", inputPath,
				"-t",
				"-b",
				"-o", afOutputPath);
		result.addGeneratedFile(afOutputPath);
		
		result.addSourceArtifact(afOutputPath);
		ExecutionResult er2 = StrategoExecutor.runStrategoCLI(context.toolsContext(), 
				main_pp_pp_table_0_0.instance, "main-pp-pp-table", new LoggingFilteringIOAgent(),
				"-i", afOutputPath,
				"-o", ppOutputPath);
		result.addGeneratedFile(ppOutputPath);
		
		if (!FileCommands.exists(afOutputPath)) {
			FileCommands.writeToFile(afOutputPath, "PP-Table([])");
			result.addGeneratedFile(afOutputPath);
		}
		
		result.setState(State.finished(er1.success && er2.success));
	}

}
