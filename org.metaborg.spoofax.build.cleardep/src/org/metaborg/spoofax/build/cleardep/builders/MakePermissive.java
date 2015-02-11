package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.permissivegrammars.make_permissive;
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
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class MakePermissive extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, MakePermissive> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, MakePermissive>() {
		@Override
		public MakePermissive makeBuilder(SpoofaxBuildContext context) { return new MakePermissive(context); }
	};
	
	public MakePermissive(SpoofaxBuildContext context) {
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
		Log.log.beginInlineTask("Make grammar permissive for error-recovery parsing.", Log.CORE); 
		
		CompilationUnit packSdf = context.packSdf.require(null, context.basePath("${include}/build.packSdf.dep"), new SimpleMode());
		result.addModuleDependency(packSdf);
		
		if (context.props.isDefined("externaldef"))
			copySdf(result);
		
		RelativePath inputPath = context.basePath("${include.rel}/${sdfmodule}.def");
		RelativePath outputPath = context.basePath("${include.rel}/${sdfmodule}-Permissive.def");
		
		result.addSourceArtifact(inputPath);
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.permissiveGrammarsContext(), 
				make_permissive.getMainStrategy(), "make-permissive", new LoggingFilteringIOAgent(Pattern.quote("[ make-permissive | info ]") + ".*"),
				"-i", inputPath,
				"-o", outputPath,
				"--optimize", "on"
				);
		result.addGeneratedFile(outputPath);
		result.setState(State.finished(er.success));
		
		Log.log.endTask();
	}

	private void copySdf(SimpleCompilationUnit result) throws IOException {
		// XXX need to `preservelastmodified`?
		Path source = new AbsolutePath(context.props.get("externaldef"));
		Path target = context.basePath("${include}/${sdfmodule}.def");
		result.addExternalFileDependency(source);
		FileCommands.copyFile(source, target);
		result.addGeneratedFile(target);
	}

}
