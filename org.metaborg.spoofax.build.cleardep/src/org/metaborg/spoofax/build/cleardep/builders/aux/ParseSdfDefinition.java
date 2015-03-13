package org.metaborg.spoofax.build.cleardep.builders.aux;

import java.io.File;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.stratego_sdf.parse_sdf_definition_file_0_0;
import org.sugarj.cleardep.build.BuildRequest;
import org.sugarj.cleardep.stamp.FileHashStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class ParseSdfDefinition extends SpoofaxBuilder<ParseSdfDefinition.Input, IStrategoTerm> {
	
	public final static SpoofaxBuilderFactory<Input, IStrategoTerm, ParseSdfDefinition> factory = new SpoofaxBuilderFactory<ParseSdfDefinition.Input, IStrategoTerm, ParseSdfDefinition>() {
		private static final long serialVersionUID = -2345729926864071894L;

		@Override
		public ParseSdfDefinition makeBuilder(Input input) {
			return new ParseSdfDefinition(input);
		}
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -4790160594622807382L;

		public final Path defPath;
		public final BuildRequest<?,?,?,?>[] requiredUnits;
		public Input(SpoofaxContext context, Path defPath, BuildRequest<?, ?, ?, ?>[] requiredUnits) {
			super(context);
			this.defPath = defPath;
			this.requiredUnits = requiredUnits;
		}
	}
	
	public ParseSdfDefinition(Input input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Parse SDF definition";
	}
	
	@Override
	protected Stamper defaultStamper() {
		return FileHashStamper.instance;
	}

	@Override
	protected Path persistentPath() {
		String rel = FileCommands.tryGetRelativePath(input.defPath);
		String relname = rel.replace(File.separatorChar, '_');
		return new RelativePath(new AbsolutePath(FileCommands.TMP_DIR), "parse.sdf." + relname + ".dep");
	}

	@Override
	protected IStrategoTerm build() throws Throwable {
		requireBuild(input.requiredUnits);
		
		require(input.defPath);
		if (!FileCommands.exists(input.defPath))
			return null;

		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		ExecutionResult er = StrategoExecutor.runStratego(false, StrategoExecutor.strategoSdfcontext(), 
				parse_sdf_definition_file_0_0.instance, "parse-sdf-definition", new LoggingFilteringIOAgent(),
				factory.makeString(input.defPath.getAbsolutePath()));
		
		return er.result;
	}
}
