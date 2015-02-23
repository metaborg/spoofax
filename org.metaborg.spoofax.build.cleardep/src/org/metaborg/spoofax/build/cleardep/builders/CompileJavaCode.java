package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.buildjava.JavaBuilder;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;

public class CompileJavaCode extends SpoofaxBuilder<SpoofaxInput> {

	public static SpoofaxBuilderFactory<SpoofaxInput, CompileJavaCode> factory = new SpoofaxBuilderFactory<SpoofaxInput, CompileJavaCode>() {
		private static final long serialVersionUID = 5448125602790119713L;

		@Override
		public CompileJavaCode makeBuilder(SpoofaxInput input, BuildManager manager) { return new CompileJavaCode(input, manager); }
	};
	
	public CompileJavaCode(SpoofaxInput input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Compile Java code";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("compileJavaCode.dep");
	}

	@Override
	public void build(CompilationUnit result) throws IOException {
		require(CopyUtils.factory, input);
		
		Path targetDir = context.basePath("${build}");
		boolean debug = true;
		String sourceVersion = "1.7";
		String targetVersion = "1.7";
		List<String> additionalArgs = new ArrayList<>();
		if (debug)
			additionalArgs.add("-g");
		additionalArgs.add("-source");
		additionalArgs.add(sourceVersion);
		additionalArgs.add("-target");
		additionalArgs.add(targetVersion);
		
		String srcDirs = context.props.getOrElse("src-dirs", context.props.get("src-gen"));
		List<Path> sourcePath = new ArrayList<>();
		List<Path> sourceFiles = new ArrayList<>();
		for (String dir : srcDirs.split("[\\s]+")) {
			Path p;
			if (AbsolutePath.acceptable(dir))
				p = new AbsolutePath(dir);
			else
				p = context.basePath(dir);
			
			sourcePath.add(p);
			sourceFiles.addAll(FileCommands.listFilesRecursive(p, new FileExtensionFilter("java")));
		}
		
		
		List<Path> classPath = new ArrayList<>();
		classPath.add(new AbsolutePath(context.props.getOrFail("eclipse.spoofaximp.strategominjar")));
		classPath.add(context.basePath("${src-gen}"));
		if (context.props.isDefined("externaljar"))
			classPath.add(new AbsolutePath(context.props.get("externaljar")));
		if (context.props.isDefined("externaljarx"))
			classPath.add(new AbsolutePath(context.props.get("externaljarx")));
		if (context.isJavaJarEnabled(result))
			classPath.add(context.basePath("${include}/${strmodule}-java.jar"));

		require(JavaBuilder.factory, 
				new JavaBuilder.Input(
						sourceFiles,
						targetDir,
						sourcePath, 
						classPath,
						additionalArgs,
						null));
	}
}
