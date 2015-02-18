package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.tools.main_rtg2sig_0_0;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Rtg2Sig extends Builder<SpoofaxBuildContext, Rtg2Sig.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Rtg2Sig> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Rtg2Sig>() {
		@Override
		public Rtg2Sig makeBuilder(SpoofaxBuildContext context) { return new Rtg2Sig(context); }
	};
	
	public static class Input {
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(String sdfmodule, String buildSdfImports) {
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Rtg2Sig(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Generate Stratego signatures for grammar constructors";
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return context.depPath("rtg2Sig." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		
		if (context.isBuildStrategoEnabled(result)) {
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang.rtg'.
			CompilationUnit sdf2Rtg = context.sdf2Rtg.require(new Sdf2Rtg.Input(input.sdfmodule, input.buildSdfImports), new SimpleMode());
			result.addModuleDependency(sdf2Rtg);

			
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
