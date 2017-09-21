package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.metaborg.sdf2parenthesize.parenthesizer.Parenthesizer;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;

import com.google.common.io.Files;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;


        public final File inputFile;
        public final File outputFile;
        public final String inputModule;


        public Input(SpoofaxContext context, File inputFile, File outputFile, String inputModule) {
            super(context);
            this.inputFile = inputFile;
            this.outputFile = outputFile;
            this.inputModule = inputModule;
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
        String fileName = input.inputFile.getName();
        return context.depPath("sdf2parenthesize-java." + fileName + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        require(input.inputFile);
        boolean status = true;

        try {
            InputStream out = Files.asByteSource(input.inputFile).openStream();
            ObjectInputStream ois = new ObjectInputStream(out);
            // read persisted normalized grammar
            ParseTable table = (ParseTable) ois.readObject();
            Parenthesizer.generateParenthesizer(input.inputModule, input.outputFile, table.normalizedGrammar());
            ois.close();
            out.close();
        } catch(Exception e) {
            System.out.println("Failed to generate parenthesizer");
            e.printStackTrace();
            status = false;
        }
        provide(input.outputFile);

        setState(State.finished(status));
        return OutputPersisted.of(input.outputFile);
    }
}
