package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.tools.main_rtg2sig_0_0;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Rtg2Sig extends SpoofaxBuilder<Rtg2Sig.Input> {

	public static SpoofaxBuilderFactory<Input, Rtg2Sig> factory = new SpoofaxBuilderFactory<Input, Rtg2Sig>() {
		private static final long serialVersionUID = -2453863767591818617L;

		@Override
		public Rtg2Sig makeBuilder(Input input, BuildManager manager) { return new Rtg2Sig(input, manager); }
	};
	
	public static class Input extends SpoofaxInput {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8305692591357842018L;
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Rtg2Sig(Input input, BuildManager manager) {
		super(input, factory, manager);
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
	public void build(CompilationUnit result) throws IOException {
		
		if (context.isBuildStrategoEnabled(result)) {
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang.rtg'.
			require(Sdf2Rtg.factory, new Sdf2Rtg.Input(context, input.sdfmodule, input.buildSdfImports));

			RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + ".rtg");
			RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".str");
			
			result.addSourceArtifact(inputPath);
			ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
					main_rtg2sig_0_0.instance, "rtg2sig", new LoggingFilteringIOAgent(),
					"-i", inputPath,
					"--module", input.sdfmodule,
					"-o", outputPath);
			result.addGeneratedFile(outputPath);
			result.setState(State.finished(er.success));
		}
	}

}
