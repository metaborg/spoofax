package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.io.NormGrammarReader;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;

import com.google.common.collect.Lists;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.output.OutputPersisted;

public class PackNormalizedSdf extends SpoofaxBuilder<PackNormalizedSdf.Input, OutputPersisted<NormGrammar>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final File inputMainNormSdfFile;
        public final Collection<LanguageIdentifier> sourceDeps;
        public final boolean isCompletions;

        public Input(SpoofaxContext context, File inputMainNormSdfFile, Collection<LanguageIdentifier> sourceDeps, boolean isCompletions) {
            super(context);
            this.inputMainNormSdfFile = inputMainNormSdfFile;
            this.sourceDeps = sourceDeps;
            this.isCompletions = isCompletions;
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
        return "Pack normalized grammar files" + (input.isCompletions ? " (completions)" : "");
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("pack-normalized-sdf-java." + input.inputMainNormSdfFile.getName() + ".dep");
    }

    @Override public OutputPersisted<NormGrammar> build(Input input) throws Exception {
        try {
            List<String> paths = srcGenNormalizedSdf3Paths(input.sourceDeps);
            
            NormGrammarReader normGrammarReader = new NormGrammarReader(paths);
            
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
    
    private List<String> srcGenNormalizedSdf3Paths(Collection<LanguageIdentifier> sourceDeps) {
        File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());
        
        final List<String> paths = Lists.newLinkedList();
        
        paths.add(srcGenSyntaxDir.getAbsolutePath());

        for(LanguageIdentifier langId : sourceDeps) {
            ILanguageImpl lang = context.languageService().getImpl(langId);
            for(final ILanguageComponent component : lang.components()) {
                ILanguageComponentConfig config = component.config();
                Collection<IExportConfig> exports = config.exports();
                for(IExportConfig exportConfig : exports) {
                    exportConfig.accept(new IExportVisitor() {
                        @Override public void visit(LangDirExport export) {
                            if(export.language.equals(SpoofaxConstants.LANG_ATERM_NAME)) {
                                try {
                                    paths
                                        .add(toFileReplicate(component.location().resolveFile(export.directory))
                                            .getAbsolutePath());
                                } catch(FileSystemException e) {
                                    System.out.println("Failed to locate path");
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override public void visit(LangFileExport export) {
                            // Ignore file exports
                        }

                        @Override public void visit(ResourceExport export) {
                            // Ignore resource exports
                        }
                    });
                }
            }
        }
        
        return paths;
    }
}
