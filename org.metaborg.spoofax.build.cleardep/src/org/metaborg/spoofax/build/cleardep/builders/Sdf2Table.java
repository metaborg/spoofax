package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input> {

	public static SpoofaxBuilderFactory<Input, Sdf2Table> factory = new SpoofaxBuilderFactory<Input, Sdf2Table>() {
		@Override
		public Sdf2Table makeBuilder(Input input) { return new Sdf2Table(input); }
	};
	
	public static class Input extends SpoofaxInput {
		public final String sdfmodule;
		public final String buildSdfImports;
		public final Path externaldef;
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, Path externaldef) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.externaldef = externaldef;
		}
	}
	
	public Sdf2Table(Input input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Compile grammar to parse table";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("sdf2Table." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		require(MakePermissive.factory, new MakePermissive.Input(context, input.sdfmodule, input.buildSdfImports, input.externaldef), new SimpleMode());

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
