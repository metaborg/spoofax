package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.sugarj.cleardep.BuildUnit;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input> {


	public static SpoofaxBuilderFactory<Input, Sdf2Table> factory = new SpoofaxBuilderFactory<Input, Sdf2Table>() {
		private static final long serialVersionUID = -5551917492018980172L;

		@Override
		public Sdf2Table makeBuilder(Input input, BuildManager manager) { return new Sdf2Table(input, manager); }
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
	
	public Sdf2Table(Input input, BuildManager manager) {
		super(input, factory, manager);
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
	public void build(BuildUnit result) throws IOException {
		require(MakePermissive.factory, new MakePermissive.Input(context, input.sdfmodule, input.buildSdfImports, input.externaldef));

		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + "-Permissive.def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".tbl");

		result.requires(inputPath);
		ExecutionResult er = StrategoExecutor.runSdf2TableCLI(StrategoExecutor.xtcContext(), 
				"-t",
				"-i", inputPath,
				"-m", input.sdfmodule,
				"-o", outputPath);
		
		result.generates(outputPath);
		result.setState(State.finished(er.success));
	}

}
