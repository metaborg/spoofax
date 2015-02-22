package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.tools.main_rtg2sig_0_0;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Rtg2Sig extends SpoofaxBuilder<Rtg2Sig.Input> {

	public static SpoofaxBuilderFactory<Input, Rtg2Sig> factory = new SpoofaxBuilderFactory<Input, Rtg2Sig>() {
		@Override
		public Rtg2Sig makeBuilder(Input input) { return new Rtg2Sig(input); }
	};
	
	public static class Input extends SpoofaxInput {
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Rtg2Sig(Input input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Generate Stratego signatures for grammar constructors";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("rtg2Sig." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		
		if (context.isBuildStrategoEnabled(result)) {
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang.rtg'.
			require(Sdf2Rtg.factory, new Sdf2Rtg.Input(context, input.sdfmodule, input.buildSdfImports), new SimpleMode());

			RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + ".rtg");
			RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".str");
			
			result.addSourceArtifact(inputPath);
			ExecutionResult er = StrategoExecutor.runStrategoCLI(context.toolsContext(), 
					main_rtg2sig_0_0.instance, "rtg2sig", new LoggingFilteringIOAgent(),
					"-i", inputPath,
					"--module", input.sdfmodule,
					"-o", outputPath);
			result.addGeneratedFile(outputPath);
			result.setState(State.finished(er.success));
		}
	}

}
