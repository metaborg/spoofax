package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.build.cleardep.util.FileNameFilter;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
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
		Log.log.beginInlineTask("Compile grammar to parse table", Log.CORE); 

		String sdfmodule = context.props.get("metasdfmodule");
		String sdfImports = context.props.substitute("-Idef \"${eclipse.spoofaximp.jars}/StrategoMix.def\" ${build.sdf.imports}");
		CompilationUnit makePermissive = context.sdf2Table.require(new Sdf2Table.Input(sdfmodule, sdfImports), new SimpleMode());
		result.addModuleDependency(makePermissive);

//		<target name="meta-sdf2table" if="metasdfmodule.available">
//		<fail unless="eclipse.spoofaximp.jars" message="Property eclipse.spoofaximp.jars must point to the directory containing StrategoMix.def" />
//		<antcall target="sdf2table">
//			<param name="sdfmodule" value="${metasdfmodule}" />
//			<param name="build.sdf.imports" value="-Idef &quot;${eclipse.spoofaximp.jars}/StrategoMix.def&quot; ${build.sdf.imports}" />
//		</antcall>
//		<antcall target="meta-sdf2table.helper" />
//	</target>
//
//	<target name="meta-sdf2table.helper" if="eclipse.running">
//		<eclipse.convertPath fileSystemPath="${include}" property="includeresource" />
//		<eclipse.refreshLocal resource="${includeresource}/${metasdfmodule}.tbl" depth="infinite" />
//	</target>
		
		boolean success = true;
		for (RelativePath inputPath : FileCommands.listFiles(context.basePath("${include}"), new FileNameFilter("-Permissive.def"))) {
			String name = FileCommands.fileName(inputPath);
			String inputBasename = name.substring(0, name.length() - "-Permissive.def".length());
			RelativePath outputPath = context.basePath("${include}/" + inputBasename + ".tbl");

			result.addSourceArtifact(inputPath);
			ExecutionResult er = StrategoExecutor.runSdf2TableCLI(context.xtcContext(), 
					"-t",
					"-i", inputPath,
					"-m", context.props.getOrFail("sdfmodule"),
					"-o", outputPath);
			
			result.addGeneratedFile(outputPath);
			success = success && er.success;
		}

		result.setState(State.finished(success));

		Log.log.endTask();
	}

}
