package org.metaborg.spoofax.build.cleardep.builders;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class StrategoJavaCompiler extends Builder<SpoofaxBuildContext, StrategoJavaCompiler.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, StrategoJavaCompiler> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, StrategoJavaCompiler>() {
		@Override
		public StrategoJavaCompiler makeBuilder(SpoofaxBuildContext context) { return new StrategoJavaCompiler(context); }
	};
	
	public static class Input {
		public final RelativePath inputPath;
		public final RelativePath outputPath;
		public final String externaljarflags;
		public final String buildStrategoArgs;
		public final Path externalDef;
		public Input(RelativePath inputPath, RelativePath outputPath, String externaljarflags, String buildStrategoArgs, Path externalDef) {
			this.inputPath = inputPath;
			this.outputPath = outputPath;
			this.externaljarflags = externaljarflags;
			this.buildStrategoArgs = buildStrategoArgs;
			this.externalDef = externalDef;
		}
	}
	
	public StrategoJavaCompiler(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Compile Stratego code";
	}
	
	@Override
	public Path persistentPath(Input input) {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.inputPath);
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("strategoJavaCompiler." + relname + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		result.addSourceArtifact(input.inputPath);
		
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.strjContext(), 
				org.strategoxt.strj.main_0_0.instance, "strj", new LoggingFilteringIOAgent(),
				"-i", input.inputPath,
				"-o", input.outputPath,
				"-p", "trans",
				"--library",
				"--clean",
				input.buildStrategoArgs,
				input.externaljarflags,
				input.externalDef != null ? "-I " + input.externalDef : "",
				"-I", context.basePath("${lib}"),
				"-I", context.basePath("${include}"),
				"-I", context.baseDir,
				"-I", context.basePath("${trans}"),
				"--cache-dir", context.basePath(".cache"));
		
		result.addGeneratedFile(input.outputPath);
		Path strdep = FileCommands.replaceExtension(input.outputPath, "dep");
		// TODO extract dependencies from `strdep`
		
		result.setState(State.finished(er.success));
	}
}
