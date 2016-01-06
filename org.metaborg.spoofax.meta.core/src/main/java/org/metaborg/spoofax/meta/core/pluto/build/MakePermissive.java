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
import build.pluto.output.None;

public class MakePermissive extends SpoofaxBuilder<MakePermissive.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 4381601872931676757L;

        public final String sdfModule;
        public final Arguments sdfArgs;


        public Input(SpoofaxContext context, String sdfModule, Arguments sdfArgs) {
            super(context);
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, MakePermissive> factory = SpoofaxBuilderFactoryFactory.of(
        MakePermissive.class, Input.class);


    public MakePermissive(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, MakePermissive, SpoofaxBuilderFactory<Input, None, MakePermissive>>
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
        return context.depPath("make-permissive." + context.settings.sdfName() + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        requireBuild(PackSdf.factory, new PackSdf.Input(context, input.sdfModule, input.sdfArgs));

        final File inputPath = toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
        final File outputPath = toFile(context.settings.getSdfCompiledPermissiveDefFile(input.sdfModule));

        require(inputPath);
        
        // @formatter:off
        final Arguments arguments = new Arguments()
            .addFile("-i", inputPath)
            .addFile("-o", outputPath)
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

        provide(outputPath);

        setState(State.finished(result.success));
        return None.val;
    }
}
