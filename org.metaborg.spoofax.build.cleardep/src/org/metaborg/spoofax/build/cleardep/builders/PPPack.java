package org.metaborg.spoofax.build.cleardep.builders;


import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.Log;
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
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		Log.log.beginTask("Prepare editor-service pretty-print table", Log.CORE);
		
		result.addSourceArtifact(input.ppInput);
		try {
			Context context = org.strategoxt.tools.tools.init();
			context.invokeStrategyCLI(org.strategoxt.tools.main_parse_pp_table_0_0.instance, "parse-pp-table", 
					"-i", input.ppInput.getAbsolutePath(),
					"-o", input.ppTermOutput.getAbsolutePath());
		} catch (StrategoExit e) {
			if (e.getValue() != 0)
				throw e;
		}
		result.addGeneratedFile(input.ppTermOutput);
		
		Log.log.endTask();
	}

}
