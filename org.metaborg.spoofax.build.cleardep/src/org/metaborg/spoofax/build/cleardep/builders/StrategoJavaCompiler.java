package org.metaborg.spoofax.build.cleardep.builders;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
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
		
		Path rtree = FileCommands.replaceExtension(input.outputPath, "rtree");
		Path strdep = FileCommands.addExtension(input.outputPath, "dep");

		FileCommands.delete(rtree);
		
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.strjContext(), 
				org.strategoxt.strj.main_0_0.instance, "strj", 
				new LoggingFilteringIOAgent(
						Pattern.quote("[ strj | info ]") + ".*", 
                        Pattern.quote("[ strj | error ] Compilation failed") + ".*",
                        Pattern.quote("[ strj | warning ] Nullary constructor") + ".*"),
				"-i", input.inputPath,
				"-o", input.outputPath,
				"-p", "trans",
				"--library",
				"--clean",
				input.buildStrategoArgs,
				input.externaljarflags,
				input.externalDef != null ? "-I " + input.externalDef : "",
				"-I", context.baseDir,
				"-I", context.basePath("${lib}"),
				"-I", context.basePath("${include}"),
				"-I", context.basePath("${trans}"),
				"--cache-dir", context.basePath(".cache"));
		FileCommands.delete(rtree);
		
		result.addGeneratedFile(input.outputPath);
		result.addGeneratedFile(rtree);
		result.addGeneratedFile(strdep);
		
		registerUsedPaths(result, strdep);
		
		result.setState(State.finished(er.success));
	}
	
	private void registerUsedPaths(CompilationUnit result, Path strdep) throws IOException {
		String contents = FileCommands.readFileAsString(strdep);
		String[] lines = contents.split("[\\s\\\\]+");
		for (int i = 1; i < lines.length; i++) { // skip first line, which lists the generated ctree file
			String line = lines[i];
			Path p = new AbsolutePath(line);
			RelativePath prel = FileCommands.getRelativePath(context.baseDir, p);
			result.addExternalFileDependency(prel != null ? prel : p);
		}
	}
}
