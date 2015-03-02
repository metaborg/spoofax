package org.metaborg.spoofax.build.cleardep.builders;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.build.BuildRequest;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class StrategoJavaCompiler extends SpoofaxBuilder<StrategoJavaCompiler.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, StrategoJavaCompiler> factory = new SpoofaxBuilderFactory<Input, None, StrategoJavaCompiler>() {
		private static final long serialVersionUID = -3539649828941211263L;

		@Override
		public StrategoJavaCompiler makeBuilder(Input input) { return new StrategoJavaCompiler(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -5234502421638344690L;
		
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
		public final BuildRequest<?,?,?,?>[] requiredUnits;
		
		public Input(
				SpoofaxContext context,
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
				BuildRequest<?,?,?,?>[] requiredUnits) {
			super(context);
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
	
	public StrategoJavaCompiler(Input input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Compile Stratego code";
	}
	
	@Override
	public Path persistentPath() {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.inputPath);
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("strategoJavaCompiler." + relname + ".dep");
	}

	@Override
	public None build() throws IOException {
		if (input.requiredUnits != null)
			for (BuildRequest<?,?,?,?> req : input.requiredUnits)
				require(req);
		
		requires(input.inputPath);
		
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
		
		
		ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.strjContext(), 
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
		
		generates(input.outputPath);
		generates(rtree);
		generates(strdep);
		
		registerUsedPaths(strdep);
		
		setState(State.finished(er.success));
		
		return None.val;
	}
	
	private void registerUsedPaths(Path strdep) throws IOException {
		String contents = FileCommands.readFileAsString(strdep);
		String[] lines = contents.split("[\\s\\\\]+");
		for (int i = 1; i < lines.length; i++) { // skip first line, which lists the generated ctree file
			String line = lines[i];
			Path p = new AbsolutePath(line);
			RelativePath prel = FileCommands.getRelativePath(context.baseDir, p);
			requires(prel != null ? prel : p);
		}
	}
}
