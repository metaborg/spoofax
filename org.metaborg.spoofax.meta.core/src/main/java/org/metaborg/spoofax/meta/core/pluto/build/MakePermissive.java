package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.permissivegrammars.make_permissive;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class MakePermissive extends SpoofaxBuilder<MakePermissive.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 4381601872931676757L;

        public final File outputPath;
        public final String depFilename;
        public final PackSdf.Input packSdfInput;

        public Input(SpoofaxContext context, File outputPath, String depFilename, PackSdf.Input packSdfInput) {
            super(context);
            this.outputPath = outputPath;
            this.depFilename = depFilename;
            this.packSdfInput = packSdfInput;
        }
        
        
        public String sdfModule() {
            return packSdfInput.sdfModule;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, MakePermissive> factory =
        SpoofaxBuilderFactoryFactory.of(MakePermissive.class, Input.class);


    public MakePermissive(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, MakePermissive, SpoofaxBuilderFactory<Input, OutputPersisted<File>, MakePermissive>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Make grammar permissive for error-recovery parsing.";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath(input.depFilename);
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        final OutputPersisted<File> out = requireBuild(PackSdf.factory, input.packSdfInput);
        final File inputPath = out.val();

        require(inputPath);

        // @formatter:off
        final Arguments arguments = new Arguments()
            .addFile("-i", inputPath)
            .addFile("-o", input.outputPath)
            .addLine("--optimize on")
            ;

        final ExecutionResult result = new StrategoExecutor()
            .withPermissiveGrammarsContext()
            .withStrategy(make_permissive.getMainStrategy())
            .withTracker(newResourceTracker(Pattern.quote("[ make-permissive | info ]") + ".*"))
            .withName("make-permissive")
            .executeCLI(arguments)
            ;
        // @formatter:on 

        provide(input.outputPath);

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputPath);
    }
}
