package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.tools.main_rtg2sig_0_0;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class Rtg2Sig extends SpoofaxBuilder<Rtg2Sig.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -8305692591357842018L;

        public final String sdfModule;
        public final Arguments sdfArgs;


        public Input(SpoofaxContext context, String sdfModule, Arguments sdfArgs) {
            super(context);
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, Rtg2Sig> factory = SpoofaxBuilderFactoryFactory.of(Rtg2Sig.class,
        Input.class);


    public Rtg2Sig(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, Rtg2Sig, SpoofaxBuilderFactory<Input, None, Rtg2Sig>> request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Generate Stratego signatures for grammar constructors";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("rtg2sig." + input.sdfModule + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        if(context.isBuildStrategoEnabled(this)) {
            requireBuild(Sdf2Rtg.factory, new Sdf2Rtg.Input(context, input.sdfModule, input.sdfArgs));

            final File inputPath = toFile(context.settings.getRtgFile(input.sdfModule));
            final File outputPath = toFile(context.settings.getStrCompiledSigFile(input.sdfModule));

            // @formatter:off
            final Arguments arguments = new Arguments()
                .addFile("-i", inputPath)
                .addAll("--module", input.sdfModule)
                .addFile("-o", outputPath)
                ;
            
            final ExecutionResult result = new StrategoExecutor()
                .withToolsContext()
                .withStrategy(main_rtg2sig_0_0.instance)
                .withTracker(newResourceTracker())
                .withName("rtg2sig")
                .executeCLI(arguments)
                ;
            // @formatter:on 

            provide(outputPath);

            setState(State.finished(result.success));
        }

        return None.val;
    }
}
