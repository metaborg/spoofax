package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.meta.core.pluto.build.misc.PrepareNativeBundle;
import org.metaborg.spoofax.meta.core.pluto.build.misc.PrepareNativeBundle.Output;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;
import build.pluto.output.OutputTransient;

public class Sdf2TableCustom extends SpoofaxBuilder<Sdf2TableCustom.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final String sdfModule;
        public final File inputPath;
        public final File outputPath;
        public final Origin origin;

        public Input(SpoofaxContext context, String sdfModule, File inputPath, File outputPath, Origin origin) {
            super(context);
            this.sdfModule = sdfModule;
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.origin = origin;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2TableCustom> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2TableCustom.class, Input.class);


    public Sdf2TableCustom(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, Sdf2TableCustom, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2TableCustom>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Compile grammar to parse table";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2table." + input.sdfModule + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        final OutputTransient<Output> commandsOutput = requireBuild(PrepareNativeBundle.factory, input);
        final Output commands = commandsOutput.val();
        requireBuild(input.origin);

        require(input.inputPath);

        final ExecutionResult result =
            commands.sdf2table.run("-t", "-i", input.inputPath.getAbsolutePath(), "-m", input.sdfModule, "-o",
                input.outputPath.getAbsolutePath());

        provide(input.outputPath);

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputPath);
    }
}
