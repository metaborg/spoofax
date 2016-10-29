package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.tools.main_rtg2sig_0_0;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Rtg2Sig extends SpoofaxBuilder<Rtg2Sig.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -8305692591357842018L;

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


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Rtg2Sig> factory =
        SpoofaxBuilderFactoryFactory.of(Rtg2Sig.class, Input.class);


    public Rtg2Sig(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, Rtg2Sig, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Rtg2Sig>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Generate Stratego signatures for grammar constructors";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("rtg2sig." + input.module + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(input.origin);
        require(input.inputFile);
        
        // @formatter:off
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputFile)
            .add("--module", input.module)
            .addFile("-o", input.outputFile)
            ;
        
        final ExecutionResult result = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_rtg2sig_0_0.instance)
            .withTracker(newResourceTracker())
            .withName("rtg2sig")
            .executeCLI(arguments)
            ;
        // @formatter:on 

        provide(input.outputFile);

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputFile);
    }
}
