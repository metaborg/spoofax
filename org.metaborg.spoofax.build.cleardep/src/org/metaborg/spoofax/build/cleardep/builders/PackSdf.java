package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
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

public class PackSdf extends Builder<SpoofaxBuildContext, PackSdf.Input, SimpleCompilationUnit> {
	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, PackSdf> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, PackSdf>() {
		@Override
		public PackSdf makeBuilder(SpoofaxBuildContext context) { return new PackSdf(context); }
	};
	
	public static class Input {
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(String sdfmodule, String buildSdfImports) {
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public PackSdf(SpoofaxBuildContext context) {
		super(context);
	}
	
	@Override
	protected Path persistentPath(Input input) {
		return context.basePath("${include}/build.packSdf." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	
	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		Log.log.beginInlineTask("Pack SDF modules", Log.CORE); 
		
		copySdf2(result);
		
		RelativePath inputPath = context.basePath("${syntax.rel}/" + input.sdfmodule + ".sdf");
		RelativePath outputPath = context.basePath("${include.rel}/" + input.sdfmodule + ".def");
		String utilsInclude = FileCommands.exists(context.basePath("${utils}")) ? context.props.substitute("-I ${utils}") : "";
		
		result.addSourceArtifact(inputPath);
		
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.toolsContext(), 
				main_pack_sdf_0_0.instance, "pack-sdf", new LoggingFilteringIOAgent(Pattern.quote("  including ") + ".*"),
				"-i", inputPath,
				"-o", outputPath,
				FileCommands.exists(context.basePath("${syntax}")) ? "-I " + context.basePath("${syntax}") : "",
				FileCommands.exists(context.basePath("${lib}")) ? "-I " + context.basePath("${lib}") : "",
				utilsInclude,
				input.buildSdfImports);
		
		result.addGeneratedFile(outputPath);
		result.setState(State.finished(er.success));
		
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
