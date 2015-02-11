package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.strategoxt.tools.main_pack_sdf_0_0;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
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
		
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.toolsContext(), 
				main_pack_sdf_0_0.instance, "pack-sdf", 
				"-i", inputPath,
				"-o", outputPath,
				"-I", context.basePath("${syntax}"),
				"-I", context.basePath("${lib}"),
				utilsInclude,
				context.props.getOrElse("build.sdf.imports", ""));
		
		result.addGeneratedFile(outputPath);
		if (er.success)
			result.setState(State.SUCCESS);
		else
			result.setState(State.FAILURE);
		
		
		for (Path required : extractRequiredPaths(er.errLog))
			result.addExternalFileDependency(required);
		
		Log.log.endTask();
	}

	private List<Path> extractRequiredPaths(String errLog) {
		final String prefix = "  including ";
		final String infix = " from ";
		
		List<Path> paths = new ArrayList<>();
		for (String s : errLog.split("\\n")) {
			if (s.startsWith(prefix)) {
				String module = s.substring(prefix.length());
				int infixIndex = module.indexOf(infix);
				if (infixIndex < 0 && AbsolutePath.acceptable(module)) {
					paths.add(new AbsolutePath(s.substring(prefix.length())));
				}
				else if (infixIndex >= 0) {
					String def = module.substring(infixIndex + infix.length());
					if (AbsolutePath.acceptable(def))
						paths.add(new AbsolutePath(def));
				}
			}
		}
		return paths;
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
