package org.metaborg.spoofax.build.cleardep.builders;


import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.tools.main_pp_pp_table_0_0;
import org.strategoxt.tools.main_ppgen_0_0;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.build.EmptyBuildInput;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class PPGen extends Builder<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit, PPGen> factory = new BuilderFactory<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit, PPGen>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5586589789861139281L;

		@Override
		public PPGen makeBuilder(SpoofaxBuildContext context) { return new PPGen(context); }
	};
	
	public static class Input implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8022231454298214187L;
		public final RelativePath ppInput;
		public final RelativePath ppTermOutput;
		public Input(RelativePath ppInput, RelativePath ppTermOutput) {
			this.ppInput = ppInput;
			this.ppTermOutput = ppTermOutput;
		}
	}
	
	private PPGen(SpoofaxBuildContext context) {
		super(context, factory);
	}
	
	@Override
	protected String taskDescription(EmptyBuildInput input) {
		return "Generate pretty-print table from grammar";
	}
	
	@Override
	protected Path persistentPath(EmptyBuildInput input) {
		return context.depPath("ppGen.dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, EmptyBuildInput input) throws IOException {
		if (!context.isBuildStrategoEnabled(result))
			return;
		
		CompilationUnit packSdf = context.packSdf.require(new PackSdf.Input(context), new SimpleMode());
		result.addModuleDependency(packSdf);

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
