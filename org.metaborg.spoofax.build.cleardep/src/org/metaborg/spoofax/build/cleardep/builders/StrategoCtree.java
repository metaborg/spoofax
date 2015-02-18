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
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

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
		public final String externaljarflags;
		public final String buildStrategoArgs;
		public final Path externalDef;
		public Input(String sdfmodule, String buildSdfImports, String strmodule, Path externaljar, String externaljarflags, String buildStrategoArgs, Path externalDef) {
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.strmodule = strmodule;
			this.externaljar = externaljar;
			this.externaljarflags = externaljarflags;
			this.buildStrategoArgs = buildStrategoArgs;
			this.externalDef = externalDef;
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
		
		RelativePath inputPath = context.basePath("${trans}/" + input.strmodule + ".str");
		RelativePath outputPath = context.basePath("${include}/" + input.strmodule + ".ctree");
		CompilationUnit strategoJavaCompiler = context.strategoJavaCompiler.require(
				new StrategoJavaCompiler.Input(inputPath, outputPath, input.externaljarflags, input.buildStrategoArgs + " -F", input.externalDef), 
				new SimpleMode());
		result.addModuleDependency(strategoJavaCompiler);
	}
}
