package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.util.List;

import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.io.NormGrammarReader;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.output.OutputPersisted;

public class PackNormalizedSdf extends SpoofaxBuilder<PackNormalizedSdf.Input, OutputPersisted<NormGrammar>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final File inputMainNormSdfFile;
        public final List<String> paths;

        public Input(SpoofaxContext context, File inputMainNormSdfFile, List<String> paths) {
            super(context);
            this.inputMainNormSdfFile = inputMainNormSdfFile;
            this.paths = paths;
        }
    }

    public static SpoofaxBuilderFactory<Input, OutputPersisted<NormGrammar>, PackNormalizedSdf> factory =
        SpoofaxBuilderFactoryFactory.of(PackNormalizedSdf.class, Input.class);

    public PackNormalizedSdf(Input input) {
        super(input);
    }

    public static
        BuildRequest<Input, OutputPersisted<NormGrammar>, PackNormalizedSdf, SpoofaxBuilderFactory<Input, OutputPersisted<NormGrammar>, PackNormalizedSdf>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    @Override protected String description(Input input) {
        return "Pack normalized grammar files";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("pack-normalized-sdf-java." + input.inputMainNormSdfFile.getName() + ".dep");
    }

    @Override public OutputPersisted<NormGrammar> build(Input input) throws Exception {
        try {
            NormGrammarReader normGrammarReader = new NormGrammarReader(input.paths);
            
            normGrammarReader.accept(this::require);
            
            NormGrammar normGrammar = normGrammarReader.readGrammar(input.inputMainNormSdfFile);
            
            setState(State.SUCCESS);
            
            return OutputPersisted.of(normGrammar);
        } catch(Exception e) {
            System.out.println("Failed to pack normalized parse table");
            e.printStackTrace();
            
            setState(State.FAILURE);
            
            throw e;
        }
    }
}
