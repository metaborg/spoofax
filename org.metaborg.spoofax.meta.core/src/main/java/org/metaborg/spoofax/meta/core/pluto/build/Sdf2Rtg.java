package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.stamp.Sdf2RtgStamper;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.tools.main_sdf2rtg_0_0;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Sdf2Rtg extends SpoofaxBuilder<Sdf2Rtg.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -4487049822305558202L;

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


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Rtg> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2Rtg.class, Input.class);


    public Sdf2Rtg(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, Sdf2Rtg, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Rtg>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Extract constructor signatures from grammar";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2rtg." + input.module + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(input.origin);

        if(SpoofaxContext.BETTER_STAMPERS) {
            require(input.inputFile, new Sdf2RtgStamper(input.context));
        } else {
            require(input.inputFile);
        }

        // @formatter:off
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputFile)
            .add("-m", input.module)
            .addFile("-o", input.outputFile)
            .add("--ignore-missing-cons")
            ;
        
        final ExecutionResult result = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_sdf2rtg_0_0.instance)
            .withTracker(newResourceTracker(Pattern.quote("Invoking native tool") + ".*"))
            .withName("sdf2rtg")
            .executeCLI(arguments)
            ;
        // @formatter:on 

        provide(input.outputFile);

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputFile);
    }
}
