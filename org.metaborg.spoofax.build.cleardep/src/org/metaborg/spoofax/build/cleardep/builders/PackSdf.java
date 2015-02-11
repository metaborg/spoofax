package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class PackSdf extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, PackSdf> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, PackSdf>() {
		@Override
		public PackSdf makeBuilder(SpoofaxBuildContext context) { return new PackSdf(context); }
	};
	
	public PackSdf(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		Log.log.beginInlineTask("Pack SDF modules", Log.CORE); 
		
		copySdf2(result);
		
		RelativePath inputPath = context.basePath("${syntax.rel}/${sdfmodule}.sdf");
		RelativePath outputPath = context.basePath("${include.rel}/${sdfmodule}.def");
		String utilsInclude = FileCommands.exists(context.basePath("${utils}")) ? context.props.substitute("-I ${utils}") : "";
		
		result.addSourceArtifact(inputPath);
		
		context.toolsContext().invokeStrategyCLI(
				org.strategoxt.tools.main_pack_sdf_0_0.instance, "parse-sdf", 
				"-i", input.ppInput.getAbsolutePath(),
				"-o", input.ppTermOutput.getAbsolutePath());
		
//		<macrodef name="pack-sdf">
//		<attribute name="input" />
//		<attribute name="output" />
//		<element name="args" optional="true" />
//		<element name="sdf-deps" optional="true" />
//		<sequential>
//			<uptodate-mio input="@{input}" output="@{output}" type="pack-sdf">
//				<action>
//					<java classname="run" failonerror="true">
//						<arg value="org.strategoxt.tools.main-pack-sdf" />
//						<arg value="-i" />
//						<arg value="${basedir}/@{input}" />
//						<arg value="-o" />
//						<arg value="${basedir}/@{output}" />
//						<args />
//					</java>
//				</action>
//				<deps>
//					<sdf-deps />
//				</deps>
//			</uptodate-mio>
//		</sequential>
//	</macrodef>
	
//		<pack-sdf input="${syntax.rel}/${sdfmodule}.sdf" output="${include.rel}/${sdfmodule}.def">
//		<sdf-deps>
//			<srcfiles dir="${basedir}" includes="**/*.sdf"/>
//			<srcfiles dir="${lib}" includes="**/*.def"/>
//			<srcfiles dir="${include}" includes="${sdfmodule}.def"/> 
//		</sdf-deps>
//	
//		<args>
//			<arg value="-I"/>
//			<arg value="${syntax}"/>
//			<arg value="-I"/>
//			<arg value="${lib}"/>
//			<arg line="${utils-include}"/>
//			<arg line="${build.sdf.imports}"/>
//		</args>
//	</pack-sdf>
		
		
		Log.log.endTask();
	}

	private void copySdf2(SimpleCompilationUnit result) {
		List<RelativePath> srcSdfFiles = FileCommands.listFilesRecursive(context.basePath("syntax"), new FileExtensionFilter("sdf"));
		for (RelativePath p : srcSdfFiles) {
			result.addSourceArtifact(p);
			// XXX need to `preservelastmodified`?
			Path target = FileCommands.copyFile(context.basePath("syntax"), context.basePath("${syntax}"), p);
			result.addGeneratedFile(target);
		}		
	}

}
