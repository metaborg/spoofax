package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.annotation.Nullable;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.permissivegrammars.make_permissive;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class MakePermissive extends SpoofaxBuilder<MakePermissive.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 4381601872931676757L;

        public final File inputFile;
        public final File outputFile;
        public final String module;
        public final @Nullable Origin origin;


        public Input(SpoofaxContext context, File inputFile, File outputFile, String module, @Nullable Origin origin) {
            super(context);
            this.inputFile = inputFile;
            this.outputFile = outputFile;
            this.module = module;
            this.origin = origin;
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
        return context.depPath("make-permissive." + input.module + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(input.origin);
        require(input.inputFile);

        // @formatter:off
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputFile)
            .addFile("-o", input.outputFile)
            .addLine("--optimize on")
            .addLine("--semantic-completions off")
            .addLine("--syntactic-completions off")
            ;

        final ExecutionResult result = new StrategoExecutor()
            .withPermissiveGrammarsContext()
            .withStrategy(make_permissive.getMainStrategy())
            .withTracker(newResourceTracker(Pattern.quote("[ make-permissive | info ]") + ".*"))
            .withName("make-permissive")
            .executeCLI(arguments)
            ;
        // @formatter:on 

        provide(input.outputFile);

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputFile);
    }
}
