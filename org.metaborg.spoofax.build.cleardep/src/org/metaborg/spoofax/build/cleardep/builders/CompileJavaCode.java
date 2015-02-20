package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.build.EmptyBuildInput;
import org.sugarj.cleardep.buildjava.JavaBuildContext;
import org.sugarj.cleardep.buildjava.JavaBuilder;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;

public class CompileJavaCode extends Builder<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit, CompileJavaCode> factory = new BuilderFactory<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit, CompileJavaCode>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7888501202167724710L;

		@Override
		public CompileJavaCode makeBuilder(SpoofaxBuildContext context) { return new CompileJavaCode(context); }
	};
	
	private CompileJavaCode(SpoofaxBuildContext context) {
		super(context, factory);
	}

	@Override
	protected String taskDescription(EmptyBuildInput input) {
		return "Compile Java code";
	}
	
	@Override
	public Path persistentPath(EmptyBuildInput input) {
		return context.depPath("compileJavaCode.dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, EmptyBuildInput input) throws IOException {
		CompilationUnit copyUtils = context.copyUtils.require(null, new SimpleMode());
		result.addModuleDependency(copyUtils);
		
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

		
		CompilationUnit javac = JavaBuilder.factory.makeBuilder(new JavaBuildContext(context.getBuildManager(), null)).require(
				new JavaBuilder.Input(
						sourceFiles,
						targetDir,
						sourcePath, 
						classPath,
						additionalArgs,
						null),
				new SimpleMode());
		result.addModuleDependency(javac);
	}
}
