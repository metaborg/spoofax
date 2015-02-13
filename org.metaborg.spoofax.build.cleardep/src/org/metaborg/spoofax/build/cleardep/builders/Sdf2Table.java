package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
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

public class Sdf2Table extends Builder<SpoofaxBuildContext, Sdf2Table.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2Table> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2Table>() {
		@Override
		public Sdf2Table makeBuilder(SpoofaxBuildContext context) { return new Sdf2Table(context); }
	};
	
	public static class Input {
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(String sdfmodule, String buildSdfImports) {
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Sdf2Table(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Compile grammar to parse table";
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return context.basePath("${include}/build.sdf2Table" + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		CompilationUnit makePermissive = context.makePermissive.require(new MakePermissive.Input(input.sdfmodule, input.buildSdfImports), new SimpleMode());
		result.addModuleDependency(makePermissive);

		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + "-Permissive.def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".tbl");

		result.addSourceArtifact(inputPath);
		ExecutionResult er = StrategoExecutor.runSdf2TableCLI(context.xtcContext(), 
				"-t",
				"-i", inputPath,
				"-m", input.sdfmodule,
				"-o", outputPath);
		
		result.addGeneratedFile(outputPath);
		result.setState(State.finished(er.success));
	}

}
