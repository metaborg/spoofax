package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.tools.main_sdf2parenthesize_0_0;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2Parenthesize extends Builder<SpoofaxBuildContext, Sdf2Parenthesize.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2Parenthesize> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2Parenthesize>() {
		@Override
		public Sdf2Parenthesize makeBuilder(SpoofaxBuildContext context) { return new Sdf2Parenthesize(context); }
	};
	
	public static class Input {
		public final String sdfmodule;
		public final String buildSdfImports;
		public final Path externaldef; 
		public Input(String sdfmodule, String buildSdfImports, Path externaldef) {
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.externaldef = externaldef;
		}
	}
	
	public Sdf2Parenthesize(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Extract parenthesis structure from grammar";
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return context.depPath("sdf2Parenthesize." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		CompilationUnit copySdf = context.copySdf.require(new CopySdf.Input(input.sdfmodule, input.externaldef), new SimpleMode());
		result.addModuleDependency(copySdf);
		
		CompilationUnit packSdf = context.packSdf.require(new PackSdf.Input(input.sdfmodule, input.buildSdfImports), new SimpleMode());
		result.addModuleDependency(packSdf);

		
		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + "-parenthesize.str");
		String outputmodule = "include/" + input.sdfmodule + "-parenthesize";

		result.addSourceArtifact(inputPath);
		// XXX avoid redundant call to sdf2table
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.toolsContext(), 
				main_sdf2parenthesize_0_0.instance, "sdf2parenthesize", new LoggingFilteringIOAgent(Pattern.quote("[ sdf2parenthesize | info ]") + ".*", Pattern.quote("Invoking native tool") + ".*"),
				"-i", inputPath,
				"-m", input.sdfmodule,
				"--lang", input.sdfmodule,
				"--omod", outputmodule,
				"-o", outputPath,
				"--main-strategy", "io-" + input.sdfmodule + "-parenthesize",
				"--rule-prefix", input.sdfmodule,
				"--sig-module", context.props.get("lib-gen") + "/" + input.sdfmodule);
		
		if (!er.success)
			FileCommands.writeToFile(outputPath, "module include/" + input.sdfmodule + "-parenthesize rules parenthesize-" + input.sdfmodule + " = id");
		
		result.addGeneratedFile(outputPath);
	}

}
