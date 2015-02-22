package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.strategoxt.tools.main_pack_sdf_0_0;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class PackSdf extends SpoofaxBuilder<PackSdf.Input> {
	
	public static SpoofaxBuilderFactory<Input, PackSdf> factory = new SpoofaxBuilderFactory<Input, PackSdf>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8067075652024253743L;

		@Override
		public PackSdf makeBuilder(Input input, BuildManager manager) { return new PackSdf(input, manager); }
	};

	public static class Input extends SpoofaxInput {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2058684747897720328L;
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(SpoofaxContext context) {
			super(context);
			this.sdfmodule = context.props.get("sdfmodule");
			this.buildSdfImports = context.props.get("build.sdf.imports");
		}
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public PackSdf(Input input, BuildManager manager) {
		super(input, factory, manager);
	}
	
	@Override
	protected String taskDescription() {
		return "Pack SDF modules";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("packSdf." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	
	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/src-gen/syntax/TemplateLang.sdf'.
		require(ForceOnSave.factory, input, new SimpleMode());
		
		copySdf2(result);
		
		RelativePath inputPath = context.basePath("${syntax}/" + input.sdfmodule + ".sdf");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
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
		for (Path required : extractRequiredPaths(er.errLog))
			result.addExternalFileDependency(required);
		
		result.setState(State.finished(er.success));
	}

	private List<Path> extractRequiredPaths(String log) {
		final String prefix = "  including ";
		final String infix = " from ";
		
		List<Path> paths = new ArrayList<>();
		for (String s : log.split("\\n")) {
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
			Path target = FileCommands.copyFile(context.basePath("syntax"), context.basePath("${syntax}"), p, StandardCopyOption.COPY_ATTRIBUTES);
			result.addGeneratedFile(target);
		}		
	}

}
