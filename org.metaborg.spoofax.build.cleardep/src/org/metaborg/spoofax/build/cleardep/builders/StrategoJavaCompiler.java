package org.metaborg.spoofax.build.cleardep.builders;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class StrategoJavaCompiler extends Builder<SpoofaxBuildContext, StrategoJavaCompiler.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, StrategoJavaCompiler> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, StrategoJavaCompiler>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5720243903011665975L;

		@Override
		public StrategoJavaCompiler makeBuilder(SpoofaxBuildContext context) { return new StrategoJavaCompiler(context); }
	};
	
	public static class Input implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6717689114039470326L;
		public final RelativePath inputPath;
		public final RelativePath outputPath;
		public final String packageName;
		public final String mainStrategy;
		public final boolean library;
		public final boolean clean;
		public final Path[] directoryIncludes;
		public final String[] libraryIncludes;
		public final Path cacheDir;
		public final String[] additionalArgs;
		public final RequirableCompilationUnit<SpoofaxBuildContext>[] requiredUnits;
		public Input(
				RelativePath inputPath,
				RelativePath outputPath,
				String packageName,
				String mainStrategy,
				boolean library,
				boolean clean,
				Path[] directoryIncludes,
				String[] libraryIncludes,
				Path cacheDir,
				String[] additionalArgs, 
				RequirableCompilationUnit<SpoofaxBuildContext>[] requiredUnits) {
			this.inputPath = inputPath;
			this.outputPath = outputPath;
			this.packageName = packageName;
			this.mainStrategy = mainStrategy;
			this.library = library;
			this.clean = clean;
			this.directoryIncludes = directoryIncludes;
			this.libraryIncludes = libraryIncludes;
			this.cacheDir = cacheDir;
			this.additionalArgs = additionalArgs;
			this.requiredUnits = requiredUnits;
		}
	}
	
	private StrategoJavaCompiler(SpoofaxBuildContext context) {
		super(context, factory);
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
		if (input.requiredUnits != null)
			for (RequirableCompilationUnit<SpoofaxBuildContext> req : input.requiredUnits)
				result.addModuleDependency(req.require(this.context));
		
		result.addSourceArtifact(input.inputPath);
		
		Path rtree = FileCommands.replaceExtension(input.outputPath, "rtree");
		Path strdep = FileCommands.addExtension(input.outputPath, "dep");

		FileCommands.delete(rtree);
		
		StringBuilder directoryIncludes = new StringBuilder();
		for (Path dir : input.directoryIncludes)
			if (dir != null)
				directoryIncludes.append("-I ").append(dir).append(" ");
		StringBuilder libraryIncludes = new StringBuilder();
		for (String lib : input.libraryIncludes)
			if (lib != null && lib.isEmpty())
				directoryIncludes.append("-la ").append(lib).append(" ");
		
		
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.strjContext(), 
				org.strategoxt.strj.main_0_0.instance, "strj", 
				new LoggingFilteringIOAgent(
						Pattern.quote("[ strj | info ]") + ".*", 
                        Pattern.quote("[ strj | error ] Compilation failed") + ".*",
                        Pattern.quote("[ strj | warning ] Nullary constructor") + ".*"),
				"-i", input.inputPath,
				"-o", input.outputPath,
				input.packageName != null ? "-p " + input.packageName : "",
				input.library ? "--library" : "",
				input.clean ? "--clean" : "",
				directoryIncludes,
				libraryIncludes,
				input.cacheDir != null ? "--cache-dir " + input.cacheDir : "",
				StringCommands.printListSeparated(input.additionalArgs, " "));
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
