package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.metaborg.newsdf2table.parsetable.ParseTableGenerator;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Sdf2TableNew extends SpoofaxBuilder<Sdf2TableNew.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final File inputFile;
        public final File outputFile;
        public final File outputContextGrammarFile;
        public final List<String> paths;
        public final boolean parenthesize;


        public Input(SpoofaxContext context, File inputFile, File outputFile, File outputContextGrammarFile, List<String> paths,
            boolean parenthesize) {
            super(context);
            this.inputFile = inputFile;
            this.outputFile = outputFile;
            this.outputContextGrammarFile = outputContextGrammarFile;
            this.paths = paths;
            this.parenthesize = parenthesize;
        }
    }

    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2TableNew> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2TableNew.class, Input.class);

    public Sdf2TableNew(Input input) {
        super(input);
    }

    public static
        BuildRequest<Input, OutputPersisted<File>, Sdf2TableNew, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2TableNew>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Compile grammar to parse table using the Java implementation";
    }

    @Override public File persistentPath(Input input) {
        String fileName = input.inputFile.getName();
        return context.depPath("sdf2table-java." + fileName + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        require(input.inputFile);
        boolean status = true;

        try {
            ParseTableGenerator pt_gen = new ParseTableGenerator(input.inputFile, input.outputFile, input.outputContextGrammarFile, input.paths, input.parenthesize);
            pt_gen.createTable();
            for(File required : pt_gen.requiredFiles()) {
                require(required);
            }           
        } catch(Exception e) {
            System.out.println("Failed to generate parse table");
            e.printStackTrace();
            status = false;
        }
        provide(input.outputFile);

        setState(State.finished(status));
        return OutputPersisted.of(input.outputFile);
    }
}
