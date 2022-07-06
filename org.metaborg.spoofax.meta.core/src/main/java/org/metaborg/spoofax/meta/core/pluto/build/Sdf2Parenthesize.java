package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.metaborg.sdf2parenthesize.parenthesizer.Parenthesizer;
import org.metaborg.sdf2table.io.ParseTableIO;
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

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, OutputPersisted<ArrayList<File>>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final BuildRequest<?, OutputPersisted<File>, ?, ?> parseTable;
        public final String module;
        public final File outputDir;

        public Input(SpoofaxContext context, BuildRequest<?, OutputPersisted<File>, ?, ?> parseTable, String module, File outputDir) {
            super(context);
            this.parseTable = parseTable;
            this.module = module;
            this.outputDir = outputDir;
        }
    }

    public static SpoofaxBuilderFactory<Input, OutputPersisted<ArrayList<File>>, Sdf2Parenthesize> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2Parenthesize.class, Input.class);

    public Sdf2Parenthesize(Input input) {
        super(input);
    }

    public static
        BuildRequest<Input, OutputPersisted<ArrayList<File>>, Sdf2Parenthesize, SpoofaxBuilderFactory<Input, OutputPersisted<ArrayList<File>>, Sdf2Parenthesize>>
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
        return context.depPath("sdf2parenthesize-java." + input.module + ".dep");
    }

    @Override public OutputPersisted<ArrayList<File>> build(Input input) throws Exception {
        OutputPersisted<File> parseTableFile = requireBuild(input.parseTable);
        require(parseTableFile.val);

        ParseTable parseTable = new ParseTableIO(parseTableFile.val).getParseTable();

        File strOutputFile = FileUtils.getFile(input.outputDir, input.module + "-parenthesize.str");
        File str2OutputFile = FileUtils.getFile(input.outputDir, input.module + "-parenthesize.str2");

        Parenthesizer.generateParenthesizer(input.module, strOutputFile, parseTable, false);
        Parenthesizer.generateParenthesizer(input.module, str2OutputFile, parseTable, true);

        provide(strOutputFile);
        provide(str2OutputFile);

        setState(State.SUCCESS);

        final ArrayList<File> result = new ArrayList<>(2);
        result.add(strOutputFile);
        result.add(str2OutputFile);
        return OutputPersisted.of(result);
    }
}
