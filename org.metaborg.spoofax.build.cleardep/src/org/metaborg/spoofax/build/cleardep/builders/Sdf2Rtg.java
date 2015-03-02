package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.build.cleardep.stampers.Sdf2RtgStamper;
import org.strategoxt.tools.main_sdf2rtg_0_0;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2Rtg extends SpoofaxBuilder<Sdf2Rtg.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Sdf2Rtg> factory = new SpoofaxBuilderFactory<Input, None, Sdf2Rtg>() {
		private static final long serialVersionUID = 7325200974940523707L;

		@Override
		public Sdf2Rtg makeBuilder(Input input) { return new Sdf2Rtg(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -4487049822305558202L;
		
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Sdf2Rtg(Input input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Extract constructor signatures from grammar";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("sdf2Rtg." + input.sdfmodule + ".dep");
	}

	@Override
	public None build() throws IOException {
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang.def'.
		require(PackSdf.factory, new PackSdf.Input(context, input.sdfmodule, input.buildSdfImports));
		
		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".rtg");

		requires(inputPath, Sdf2RtgStamper.instance.stampOf(inputPath));
		
		// XXX avoid redundant call to sdf2table
		ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_sdf2rtg_0_0.instance, "sdf2rtg", new LoggingFilteringIOAgent(Pattern.quote("Invoking native tool") + ".*"),
				"-i", inputPath,
				"-m", input.sdfmodule,
				"-o", outputPath,
				"--ignore-missing-cons" /*,
				"-Xnativepath", context.basePath("${nativepath}/")*/);
		generates(outputPath);
		setState(State.finished(er.success));
		
		return None.val;
	}

}
