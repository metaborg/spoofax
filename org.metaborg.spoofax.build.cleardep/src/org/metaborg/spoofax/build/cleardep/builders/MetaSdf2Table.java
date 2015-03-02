package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.sugarj.cleardep.None;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class MetaSdf2Table extends SpoofaxBuilder<MetaSdf2Table.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, MetaSdf2Table> factory = new SpoofaxBuilderFactory<Input, None, MetaSdf2Table>() {
		private static final long serialVersionUID = 5848449529745147614L;

		@Override
		public MetaSdf2Table makeBuilder(Input input) { return new MetaSdf2Table(input); }
	};

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -3179663405417276186L;
		
		public final String metasdfmodule;
		public final String buildSdfImports;
		public final Path externaldef;
		public Input(SpoofaxContext context, String metasdfmodule, String buildSdfImports, Path externaldef) {
			super(context);
			this.metasdfmodule = metasdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.externaldef = externaldef;
		}
	}
	
	public MetaSdf2Table(Input input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Compile metagrammar for concrete object syntax";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("metaSdf2Table." + input.metasdfmodule + ".dep");
	}

	@Override
	public None build() throws IOException {
		if (!context.props.isDefined("eclipse.spoofaximp.jars"))
			throw new IllegalArgumentException("Property eclipse.spoofaximp.jars must point to the directory containing StrategoMix.def");
		
		RelativePath metamodule = context.basePath("${syntax}/${metasdfmodule}.sdf");
		requires(metamodule);
		boolean metasdfmoduleAvailable = FileCommands.exists(metamodule);
		
		if (metasdfmoduleAvailable) {
			String sdfImports = context.props.substitute("-Idef ${eclipse.spoofaximp.jars}/StrategoMix.def ") + input.buildSdfImports;
			require(Sdf2Table.factory, new Sdf2Table.Input(context, input.metasdfmodule, sdfImports, input.externaldef));
		}
		
		return None.val;

		// TODO need to refresh here?
//	<target name="meta-sdf2table.helper" if="eclipse.running">
//		<eclipse.convertPath fileSystemPath="${include}" property="includeresource" />
//		<eclipse.refreshLocal resource="${includeresource}/${metasdfmodule}.tbl" depth="infinite" />
//	</target>
	}

}
