package org.metaborg.spoofax.build.cleardep.builders;


import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.tools.main_parse_pp_table_0_0;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class PPPack extends Builder<SpoofaxBuildContext, PPPack.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, PPPack.Input, SimpleCompilationUnit, PPPack> factory = new BuilderFactory<SpoofaxBuildContext, PPPack.Input, SimpleCompilationUnit, PPPack>() {
		@Override
		public PPPack makeBuilder(SpoofaxBuildContext context) { return new PPPack(context); }
	};
	
	public static class Input {
		public final RelativePath ppInput;
		public final RelativePath ppTermOutput;
		public Input(RelativePath ppInput, RelativePath ppTermOutput) {
			this.ppInput = ppInput;
			this.ppTermOutput = ppTermOutput;
		}
	}
	
	public PPPack(SpoofaxBuildContext context) {
		super(context);
	}
	
	@Override
	protected String taskDescription(Input input) {
		return "Prepare editor-service pretty-print table";
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return FileCommands.addExtension(input.ppTermOutput, "dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		result.addSourceArtifact(input.ppInput);
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.toolsContext(), 
				main_parse_pp_table_0_0.instance, "parse-pp-table", new LoggingFilteringIOAgent(),
					"-i", input.ppInput,
					"-o", input.ppTermOutput);
		result.addGeneratedFile(input.ppTermOutput);
		result.setState(State.finished(er.success));
	}

}
