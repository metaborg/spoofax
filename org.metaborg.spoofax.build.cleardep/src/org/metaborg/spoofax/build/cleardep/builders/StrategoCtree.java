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
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;

public class StrategoCtree extends Builder<SpoofaxBuildContext, StrategoCtree.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, StrategoCtree> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, StrategoCtree>() {
		@Override
		public StrategoCtree makeBuilder(SpoofaxBuildContext context) { return new StrategoCtree(context); }
	};
	
	public static class Input {
		public final String sdfmodule;
		public final String buildSdfImports;
		public final String strmodule;
		public final Path externaljar;
		public Input(String sdfmodule, String buildSdfImports, String strmodule, Path externaljar) {
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.strmodule = strmodule;
			this.externaljar = externaljar;
		}
	}
	
	public StrategoCtree(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Prepare Stratego code for interpretation";
	}
	
	@Override
	public Path persistentPath(Input input) {
		return context.depPath("strategoCtree." + input.sdfmodule + "." + input.strmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		CompilationUnit rtg2Sig = context.rtg2Sig.require(new Rtg2Sig.Input(input.sdfmodule, input.buildSdfImports), new SimpleMode());
		result.addModuleDependency(rtg2Sig);
		
		if (!context.isBuildStrategoEnabled(result))
			throw new IllegalArgumentException(context.props.substitute("Main stratego file '${strmodule}.str' not found."));
		
		CompilationUnit copyJar = context.copyJar.require(new CopyJar.Input(input.externaljar), new SimpleMode());
		result.addModuleDependency(copyJar);
		
		boolean strcJavaAvailable = FileCommands.exists(context.basePath("${include}/${strmodule}.ctree"));
		
		
//		<target name="stratego.ctree" depends="rtg2sig">
//		<antcall target="stratego.jvm.helper">
//			<param name="build.stratego.outputfile" value="${include}/${strmodule}.ctree" />
//			<param name="build.stratego.extraargs" value="-F" />
//		</antcall>
//	</target>

	}
}
