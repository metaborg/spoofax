package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.strategoxt.imp.metatooling.building.AntDescriptorBuilder;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Sdf2ImpEclipse extends Builder<SpoofaxBuildContext, Sdf2ImpEclipse.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2ImpEclipse> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, Sdf2ImpEclipse>() {
		@Override
		public Sdf2ImpEclipse makeBuilder(SpoofaxBuildContext context) { return new Sdf2ImpEclipse(context); }
	};
	
	public static class Input {
		public final String esvmodule;
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(String esvmodule, String sdfmodule, String buildSdfImports) {
			this.esvmodule = esvmodule;
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Sdf2ImpEclipse(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Generate Eclipse IMP plug-in";
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return context.depPath("sdf2ImpEclipse." + input.esvmodule + "." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		CompilationUnit sdf2Rtg = context.sdf2Rtg.require(new Sdf2Rtg.Input(input.sdfmodule, input.buildSdfImports), new SimpleMode());
		result.addModuleDependency(sdf2Rtg);
		
		// TODO: required files
		
		RelativePath outputPath = context.basePath("${include}/" + input.esvmodule + ".packed.esv");

		AntDescriptorBuilder.main(new String[]{outputPath.getAbsolutePath()});
		result.addGeneratedFile(outputPath);
		
		// TODO: generated files

//		result.setState(State.finished(er.success));
	}

}
