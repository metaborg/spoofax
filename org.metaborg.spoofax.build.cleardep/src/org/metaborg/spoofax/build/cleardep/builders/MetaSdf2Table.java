package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.io.Serializable;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
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

public class MetaSdf2Table extends Builder<SpoofaxBuildContext, MetaSdf2Table.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, MetaSdf2Table> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, MetaSdf2Table>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4404676336530294200L;

		@Override
		public MetaSdf2Table makeBuilder(SpoofaxBuildContext context) { return new MetaSdf2Table(context); }
	};

	public static class Input implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 485053945960594504L;
		public final String metasdfmodule;
		public final String buildSdfImports;
		public final Path externaldef;
		public Input(String metasdfmodule, String buildSdfImports, Path externaldef) {
			this.metasdfmodule = metasdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.externaldef = externaldef;
		}
	}
	
	private MetaSdf2Table(SpoofaxBuildContext context) {
		super(context, factory);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Compile metagrammar for concrete object syntax";
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return context.depPath("metaSdf2Table." + input.metasdfmodule + ".dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		RelativePath metamodule = context.basePath("${syntax}/${metasdfmodule}.sdf");
		result.addSourceArtifact(metamodule);
		boolean metasdfmoduleAvailable = FileCommands.exists(metamodule);
		
		if (metasdfmoduleAvailable) {
			String sdfImports = context.props.substitute("-Idef ${eclipse.spoofaximp.jars}/StrategoMix.def ") + input.buildSdfImports;
			CompilationUnit sdf2Table = context.sdf2Table.require(new Sdf2Table.Input(input.metasdfmodule, sdfImports, input.externaldef), new SimpleMode());
			result.addModuleDependency(sdf2Table);
		}

//	<target name="meta-sdf2table.helper" if="eclipse.running">
//		<eclipse.convertPath fileSystemPath="${include}" property="includeresource" />
//		<eclipse.refreshLocal resource="${includeresource}/${metasdfmodule}.tbl" depth="infinite" />
//	</target>
	}

}
