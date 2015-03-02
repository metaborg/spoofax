package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.imp.generator.sdf2imp_jvm_0_0;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2ImpEclipse extends SpoofaxBuilder<Sdf2ImpEclipse.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Sdf2ImpEclipse> factory = new SpoofaxBuilderFactory<Input, None, Sdf2ImpEclipse>() {
		private static final long serialVersionUID = 8374273854477950798L;

		@Override
		public Sdf2ImpEclipse makeBuilder(Input input) { return new Sdf2ImpEclipse(input); }
	};

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 5390265710389276659L;
		public final String esvmodule;
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(SpoofaxContext context, String esvmodule, String sdfmodule, String buildSdfImports) {
			super(context);
			this.esvmodule = esvmodule;
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Sdf2ImpEclipse(Input input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Generate Eclipse IMP plug-in";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("sdf2ImpEclipse." + input.esvmodule + ".dep");
	}

	@Override
	public None build() throws IOException {
		require(Sdf2Rtg.factory, new Sdf2Rtg.Input(context, input.sdfmodule, input.buildSdfImports));
		
		RelativePath inputPath = new RelativePath(context.basePath("editor"), input.esvmodule + ".main.esv");

		LoggingFilteringIOAgent agent = new LoggingFilteringIOAgent(".*");
		agent.setWorkingDir(inputPath.getBasePath().getAbsolutePath());
		
		requires(inputPath);
		ExecutionResult er = StrategoExecutor.runStratego(StrategoExecutor.generatorContext(), 
				sdf2imp_jvm_0_0.instance, "sdf2imp", agent,
				StrategoExecutor.generatorContext().getFactory().makeString(inputPath.getRelativePath()));

		registerUsedPaths(er.errLog);
		
		setState(State.finished(er.success));
		return None.val;
	}

	private void registerUsedPaths(String log) {
		String defPrefix = "Found accompanying .def file: ";
		String reqPrefix = "found file ";
		String genPrefix = "Generating ";
		
		for (String s : log.split("\\n")) {
		    if (s.startsWith(reqPrefix)) {
				String file = s.substring(reqPrefix.length());
				requires(context.basePath(file));
			}
			else if (s.startsWith(genPrefix)) {
				String file = s.substring(genPrefix.length());
				generates(context.basePath(file));
			}
			else if (s.startsWith(defPrefix)) {
				String file = s.substring(defPrefix.length());
				requires(context.basePath(file));
			}
		}
	}

}
