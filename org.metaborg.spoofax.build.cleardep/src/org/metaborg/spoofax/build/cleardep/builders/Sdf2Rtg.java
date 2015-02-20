package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.tools.main_sdf2rtg_0_0;
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

public class Sdf2Rtg extends Builder<SpoofaxBuildContext, Sdf2Rtg.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2Rtg> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2Rtg>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5837101494509348496L;

		@Override
		public Sdf2Rtg makeBuilder(SpoofaxBuildContext context) { return new Sdf2Rtg(context); }
	};
	
	public static class Input implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7624083951730823068L;
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(String sdfmodule, String buildSdfImports) {
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	private Sdf2Rtg(SpoofaxBuildContext context) {
		super(context, factory);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Extract constructor signatures from grammar";
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return context.depPath("sdf2Rtg." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang.def'.
		CompilationUnit packSdf = context.packSdf.require(new PackSdf.Input(input.sdfmodule, input.buildSdfImports), new SimpleMode());
		result.addModuleDependency(packSdf);

		
		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".rtg");

		result.addSourceArtifact(inputPath);
		// XXX avoid redundant call to sdf2table
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.toolsContext(), 
				main_sdf2rtg_0_0.instance, "sdf2rtg", new LoggingFilteringIOAgent(Pattern.quote("Invoking native tool") + ".*"),
				"-i", inputPath,
				"-m", input.sdfmodule,
				"-o", outputPath,
				"--ignore-missing-cons" /*,
				"-Xnativepath", context.basePath("${nativepath}/")*/);
		result.addGeneratedFile(outputPath);
		result.setState(State.finished(er.success));
	}

}
