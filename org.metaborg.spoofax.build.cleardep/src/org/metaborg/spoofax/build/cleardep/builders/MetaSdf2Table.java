package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

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

public class MetaSdf2Table extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, MetaSdf2Table> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, MetaSdf2Table>() {
		@Override
		public MetaSdf2Table makeBuilder(SpoofaxBuildContext context) { return new MetaSdf2Table(context); }
	};
	
	public MetaSdf2Table(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Void input) {
		return "Compile metagrammar for concrete object syntax";
	}
	
	@Override
	protected Path persistentPath(Void input) {
		return context.basePath("${include}/build.metaSdf2Table.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		RelativePath metamodule = context.basePath("${syntax}/${metasdfmodule}.sdf");
		result.addSourceArtifact(metamodule);
		boolean metasdfmoduleAvailable = FileCommands.exists(metamodule);
		
		if (metasdfmoduleAvailable) {
			String sdfmodule = context.props.get("metasdfmodule");
			String sdfImports = context.props.substitute("-Idef ${eclipse.spoofaximp.jars}/StrategoMix.def ${build.sdf.imports}");
			CompilationUnit sdf2Table = context.sdf2Table.require(new Sdf2Table.Input(sdfmodule, sdfImports), new SimpleMode());
			result.addModuleDependency(sdf2Table);
		}

//	<target name="meta-sdf2table.helper" if="eclipse.running">
//		<eclipse.convertPath fileSystemPath="${include}" property="includeresource" />
//		<eclipse.refreshLocal resource="${includeresource}/${metasdfmodule}.tbl" depth="infinite" />
//	</target>
	}

}
