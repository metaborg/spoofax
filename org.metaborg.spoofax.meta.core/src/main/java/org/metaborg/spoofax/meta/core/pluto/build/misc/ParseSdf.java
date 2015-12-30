package org.metaborg.spoofax.meta.core.pluto.build.misc;

import java.io.File;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.stratego_sdf.parse_sdf_definition_file_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.FileHashStamper;
import build.pluto.stamp.Stamper;

public class ParseSdf extends SpoofaxBuilder<ParseSdf.Input, OutputPersisted<IStrategoTerm>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -4790160594622807382L;

        public final File defPath;
        public final Origin requiredUnits;


        public Input(SpoofaxContext context, File defPath, Origin requiredUnits) {
            super(context);
            this.defPath = defPath;
            this.requiredUnits = requiredUnits;
        }
    }


    public final static SpoofaxBuilderFactory<Input, OutputPersisted<IStrategoTerm>, ParseSdf> factory =
        SpoofaxBuilderFactoryFactory.of(ParseSdf.class, Input.class);


    public ParseSdf(Input input) {
        super(input);
    }

    
    public static
        BuildRequest<Input, OutputPersisted<IStrategoTerm>, ParseSdf, SpoofaxBuilderFactory<Input, OutputPersisted<IStrategoTerm>, ParseSdf>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Parse SDF definition";
    }

    @Override protected Stamper defaultStamper() {
        return FileHashStamper.instance;
    }

    @Override public File persistentPath(Input input) {
        final String rel = input.defPath.getPath();
        final String relname = rel.replace(File.separatorChar, '_');
        return new File(new File(FileCommands.TMP_DIR), "parse-sdf." + relname + ".dep");
    }

    @Override protected OutputPersisted<IStrategoTerm> build(Input input) throws Throwable {
        requireBuild(input.requiredUnits);

        require(input.defPath);
        if(!FileCommands.exists(input.defPath))
            return null;

        final ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
        final ExecutionResult result =
            StrategoExecutor.runStratego(false, StrategoExecutor.strategoSdfcontext(),
                parse_sdf_definition_file_0_0.instance, "parse-sdf-definition", newResourceTracker(),
                factory.makeString(input.defPath.getAbsolutePath()));

        return OutputPersisted.of(result.result);
    }
}
