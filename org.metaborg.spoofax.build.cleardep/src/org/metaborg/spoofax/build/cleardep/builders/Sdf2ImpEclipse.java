package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.imp.generator.sdf2imp_jvm_0_0;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2ImpEclipse extends SpoofaxBuilder<Sdf2ImpEclipse.Input> {

	public static SpoofaxBuilderFactory<Input, Sdf2ImpEclipse> factory = new SpoofaxBuilderFactory<Input, Sdf2ImpEclipse>() {

		@Override
		public Sdf2ImpEclipse makeBuilder(Input input, BuildManager manager) { return new Sdf2ImpEclipse(input, manager); }
	};

	public static class Input extends SpoofaxInput {
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
	
	public Sdf2ImpEclipse(Input input, BuildManager manager) {
		super(input, factory, manager);
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
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		require(Sdf2Rtg.factory, new Sdf2Rtg.Input(context, input.sdfmodule, input.buildSdfImports), new SimpleMode());
		
		RelativePath inputPath = new RelativePath(context.basePath("editor"), input.esvmodule + ".main.esv");

		LoggingFilteringIOAgent agent = new LoggingFilteringIOAgent(".*");
		agent.setWorkingDir(inputPath.getBasePath().getAbsolutePath());
		
		result.addSourceArtifact(inputPath);
		ExecutionResult er = StrategoExecutor.runStratego(context.generatorContext(), 
				sdf2imp_jvm_0_0.instance, "sdf2imp", agent,
				context.generatorContext().getFactory().makeString(inputPath.getRelativePath()));

		registerUsedPaths(result, er.errLog);
		
		result.setState(State.finished(er.success));
	}

	private void registerUsedPaths(CompilationUnit result, String log) {
		String defPrefix = "Found accompanying .def file: ";
		String reqPrefix = "found file ";
		String genPrefix = "Generating ";
		
		for (String s : log.split("\\n")) {
		    if (s.startsWith(reqPrefix)) {
				String file = s.substring(reqPrefix.length());
				result.addExternalFileDependency(context.basePath(file));
			}
			else if (s.startsWith(genPrefix)) {
				String file = s.substring(genPrefix.length());
				result.addGeneratedFile(context.basePath(file));
			}
			else if (s.startsWith(defPrefix)) {
				String file = s.substring(defPrefix.length());
				result.addExternalFileDependency(context.basePath(file));
			}
		}
	}

}
