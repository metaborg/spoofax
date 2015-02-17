package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.imp.generator.sdf2imp_jvm_0_0;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2ImpEclipse extends Builder<SpoofaxBuildContext, Sdf2ImpEclipse.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2ImpEclipse> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2ImpEclipse>() {
		@Override
		public Sdf2ImpEclipse makeBuilder(SpoofaxBuildContext context) { return new Sdf2ImpEclipse(context); }
	};
	
	public static class Input {
		public final String esvmodule;
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(String esvmodule, String sdfmodule, String buildSdfImports) {
			this.esvmodule = esvmodule;
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Sdf2ImpEclipse(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Generate Eclipse IMP plug-in";
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return context.depPath("sdf2ImpEclipse." + input.esvmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		CompilationUnit sdf2Rtg = context.sdf2Rtg.require(new Sdf2Rtg.Input(input.sdfmodule, input.buildSdfImports), new SimpleMode());
		result.addModuleDependency(sdf2Rtg);
		
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
