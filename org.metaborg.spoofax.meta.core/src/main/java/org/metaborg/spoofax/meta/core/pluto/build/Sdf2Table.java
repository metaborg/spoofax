package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.misc.PrepareNativeBundle;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

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

    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Table> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2Table.class, Input.class);

    public Sdf2Table(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, Sdf2Table, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Table>>
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
        return context.depPath("sdf2table." + input.module + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(input.origin);
        final PrepareNativeBundle.Output commands =
            requireBuild(PrepareNativeBundle.factory, new PrepareNativeBundle.Input(context)).val();
        require(input.inputFile);

        // sdf2table fails when directory of output file does not exist; create it first.
        FileUtils.forceMkdir(input.outputFile.getParentFile());

        final ExecutionResult result = commands.sdf2table.run("-t", "-i", input.inputFile.getAbsolutePath(), "-m",
            input.module, "-o", input.outputFile.getAbsolutePath());

        provide(input.outputFile);

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputFile);
    }
}
