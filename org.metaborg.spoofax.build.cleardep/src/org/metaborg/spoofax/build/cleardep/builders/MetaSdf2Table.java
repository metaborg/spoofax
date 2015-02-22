package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class MetaSdf2Table extends SpoofaxBuilder<MetaSdf2Table.Input> {

	public static SpoofaxBuilderFactory<Input, MetaSdf2Table> factory = new SpoofaxBuilderFactory<Input, MetaSdf2Table>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5848449529745147614L;

		@Override
		public MetaSdf2Table makeBuilder(Input input, BuildManager manager) { return new MetaSdf2Table(input, manager); }
	};

	public static class Input extends SpoofaxInput {
		/**
		 * 
		 */
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
	
	public MetaSdf2Table(Input input, BuildManager manager) {
		super(input, factory, manager);
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
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		if (!context.props.isDefined("eclipse.spoofaximp.jars"))
			throw new IllegalArgumentException("Property eclipse.spoofaximp.jars must point to the directory containing StrategoMix.def");
		
		RelativePath metamodule = context.basePath("${syntax}/${metasdfmodule}.sdf");
		result.addSourceArtifact(metamodule);
		boolean metasdfmoduleAvailable = FileCommands.exists(metamodule);
		
		if (metasdfmoduleAvailable) {
			String sdfImports = context.props.substitute("-Idef ${eclipse.spoofaximp.jars}/StrategoMix.def ") + input.buildSdfImports;
			require(Sdf2Table.factory, new Sdf2Table.Input(context, input.metasdfmodule, sdfImports, input.externaldef), new SimpleMode());
		}

		// TODO need to refresh here?
//	<target name="meta-sdf2table.helper" if="eclipse.running">
//		<eclipse.convertPath fileSystemPath="${include}" property="includeresource" />
//		<eclipse.refreshLocal resource="${includeresource}/${metasdfmodule}.tbl" depth="infinite" />
//	</target>
	}

}
