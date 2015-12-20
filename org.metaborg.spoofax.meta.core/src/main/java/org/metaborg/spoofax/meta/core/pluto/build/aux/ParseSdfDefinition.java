package org.metaborg.spoofax.meta.core.pluto.build.aux;

import java.io.File;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.meta.core.pluto.util.LoggingFilteringIOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.stratego_sdf.parse_sdf_definition_file_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.FileHashStamper;
import build.pluto.stamp.Stamper;

public class ParseSdfDefinition extends SpoofaxBuilder<ParseSdfDefinition.Input, OutputPersisted<IStrategoTerm>> {
	
	public final static SpoofaxBuilderFactory<Input, OutputPersisted<IStrategoTerm>, ParseSdfDefinition> factory = SpoofaxBuilderFactoryFactory.of(ParseSdfDefinition.class,
			Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -4790160594622807382L;

		public final File defPath;
		public final BuildRequest<?,?,?,?>[] requiredUnits;

		public Input(SpoofaxContext context, File defPath, BuildRequest<?, ?, ?, ?>[] requiredUnits) {
			super(context);
			this.defPath = defPath;
			this.requiredUnits = requiredUnits;
		}
	}
	
	public ParseSdfDefinition(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Parse SDF definition";
	}
	
	@Override
	protected Stamper defaultStamper() {
		return FileHashStamper.instance;
	}

	@Override
    public File persistentPath(Input input) {
		String rel = input.defPath.getPath();
		String relname = rel.replace(File.separatorChar, '_');
		return new File(new File(FileCommands.TMP_DIR), "parse.sdf." + relname + ".dep");
	}

	@Override
	protected OutputPersisted<IStrategoTerm> build(Input input) throws Throwable {
		requireBuild(input.requiredUnits);
		
		require(input.defPath);
		if (!FileCommands.exists(input.defPath))
			return null;

		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		ExecutionResult er = StrategoExecutor.runStratego(false, StrategoExecutor.strategoSdfcontext(), 
				parse_sdf_definition_file_0_0.instance, "parse-sdf-definition", new LoggingFilteringIOAgent(),
				factory.makeString(input.defPath.getAbsolutePath()));
		
		return OutputPersisted.of(er.result);
	}
}
