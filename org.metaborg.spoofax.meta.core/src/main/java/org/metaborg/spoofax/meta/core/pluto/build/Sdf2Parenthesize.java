package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import org.metaborg.sdf2parenthesize.parenthesizer.Parenthesizer;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final BuildRequest<?, OutputPersisted<ParseTable>, ?, ?> parseTable;
        public final String inputModule;
        public final File outputFile;

        public Input(SpoofaxContext context, BuildRequest<?, OutputPersisted<ParseTable>, ?, ?> parseTable, String inputModule, File outputFile) {
            super(context);
            this.parseTable = parseTable;
            this.inputModule = inputModule;
            this.outputFile = outputFile;
        }
    }

    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Parenthesize> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2Parenthesize.class, Input.class);

    public Sdf2Parenthesize(Input input) {
        super(input);
    }

    public static
        BuildRequest<Input, OutputPersisted<File>, Sdf2Parenthesize, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Parenthesize>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }

    @Override protected String description(Input input) {
        return "Extract parenthesis structure from grammar using the Java implementation";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2parenthesize-java." + input.inputModule + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        OutputPersisted<ParseTable> parseTable = requireBuild(input.parseTable);
        
        Parenthesizer.generateParenthesizer(input.inputModule, input.outputFile, parseTable.val);

        provide(input.outputFile);

        setState(State.SUCCESS);
        
        return OutputPersisted.of(input.outputFile);
    }
}
