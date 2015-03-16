package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input, None> {


	public static SpoofaxBuilderFactory<Input, None, Sdf2Table> factory = new SpoofaxBuilderFactory<Input, None, Sdf2Table>() {
		private static final long serialVersionUID = -5551917492018980172L;

		@Override
		public Sdf2Table makeBuilder(Input input) { return new Sdf2Table(input); }
	};
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -2379365089609792204L;
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
	public None build() throws IOException {
		requireBuild(MakePermissive.factory, new MakePermissive.Input(context, input.sdfmodule, input.buildSdfImports, input.externaldef));

		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + "-Permissive.def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".tbl");

		require(inputPath);
		ExecutionResult er = StrategoExecutor.runSdf2TableCLI(StrategoExecutor.xtcContext(), 
				"-t",
				"-i", inputPath,
				"-m", input.sdfmodule,
				"-o", outputPath);
		
		provide(outputPath);
		setState(State.finished(er.success));
		return None.val;
	}

}
