package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input, OutputPersisted<ParseTable>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final BuildRequest<?, OutputPersisted<NormGrammar>, ?, ?> inputNormGrammar;
        public final File outputParseTableFile;
        public final File outputPersistedParseTableFile;
        public final boolean dynamic;
        public final boolean dataDependent;
        public final boolean solveDeepConflicts;
        public final boolean isCompletions;

        public Input(SpoofaxContext context, BuildRequest<?, OutputPersisted<NormGrammar>, ?, ?> inputNormGrammar, File outputParseTableFile, File outputPersistedParseTableFile, boolean dynamic, boolean dataDependent,
            boolean layoutSensitive, boolean isCompletions) {
            super(context);
            this.inputNormGrammar = inputNormGrammar;
            this.outputParseTableFile = outputParseTableFile;
            this.outputPersistedParseTableFile = outputPersistedParseTableFile;
            this.dynamic = dynamic;
            this.dataDependent = dataDependent;
            this.solveDeepConflicts = !layoutSensitive;
            this.isCompletions = isCompletions;
        }
    }

    public static SpoofaxBuilderFactory<Input, OutputPersisted<ParseTable>, Sdf2Table> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2Table.class, Input.class);

    public Sdf2Table(Input input) {
        super(input);
    }

    public static
        BuildRequest<Input, OutputPersisted<ParseTable>, Sdf2Table, SpoofaxBuilderFactory<Input, OutputPersisted<ParseTable>, Sdf2Table>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }

    @Override protected String description(Input input) {
        return "Compile normalized grammar to parse table using the Java implementation" + (input.isCompletions ? " (completions)" : "");
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2table-java." + input.outputParseTableFile.getName() + ".dep");
    }

    @Override public OutputPersisted<ParseTable> build(Input input) throws Exception {
        OutputPersisted<NormGrammar> normGrammar = requireBuild(input.inputNormGrammar);
        
        ParseTable parseTable = new ParseTable(normGrammar.val, input.dynamic, input.dataDependent, input.solveDeepConflicts);
        
        IStrategoTerm parseTableATerm = ParseTableIO.generateATerm(parseTable);
        
        ParseTableIO.outputToFile(parseTableATerm.toString(), input.outputParseTableFile);
        ParseTableIO.persistObjectToFile(parseTable, input.outputPersistedParseTableFile);

        provide(input.outputParseTableFile);
        provide(input.outputPersistedParseTableFile);

        setState(State.SUCCESS);
        
        return OutputPersisted.of(parseTable);
    }
}
