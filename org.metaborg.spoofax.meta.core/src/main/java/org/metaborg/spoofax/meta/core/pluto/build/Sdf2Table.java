package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.io.NormGrammarReader;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import mb.util.vfs2.resource.ResourceUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final Collection<File> inputNormSdfFiles;
        public final Collection<LanguageIdentifier> sourceDeps;
        public final File outputParseTableFile;
        public final File outputPersistedParseTableFile;
        public final ParseTableConfiguration tableConfig;
        public final boolean isCompletions;

        public Input(SpoofaxContext context, Collection<File> inputNormSdfFiles,
            Collection<LanguageIdentifier> sourceDeps, File outputParseTableFile, File outputPersistedParseTableFile,
            ParseTableConfiguration tableConfig, boolean isCompletions) {
            super(context);
            this.inputNormSdfFiles = inputNormSdfFiles;
            this.sourceDeps = sourceDeps;
            this.outputParseTableFile = outputParseTableFile;
            this.outputPersistedParseTableFile = outputPersistedParseTableFile;
            this.tableConfig = tableConfig;
            this.isCompletions = isCompletions;
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
        return "Compile normalized grammar to parse table using the Java implementation" + (input.isCompletions ? " (completions)" : "");
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2table-java." + input.outputParseTableFile.getName() + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws Exception {
        List<String> paths = srcGenNormalizedSdf3Paths(input.sourceDeps);
        
        NormGrammarReader normGrammarReader = new NormGrammarReader(paths);
        
        normGrammarReader.accept(this::require);
        
        for(File inputNormSdfFile : input.inputNormSdfFiles)
            normGrammarReader.readModule(inputNormSdfFile);

        NormGrammar normGrammar = normGrammarReader.getGrammar();

        ParseTable parseTable = new ParseTable(normGrammar, input.tableConfig);
        IStrategoTerm parseTableATerm = ParseTableIO.generateATerm(parseTable);
        
        ParseTableIO.outputToFile(parseTableATerm, input.outputParseTableFile);
        ParseTableIO.persistObjectToFile(parseTable, input.outputPersistedParseTableFile);
        
        provide(input.outputParseTableFile);
        provide(input.outputPersistedParseTableFile);

        setState(State.SUCCESS);
        
        return OutputPersisted.of(input.outputPersistedParseTableFile);
    }
    
    private List<String> srcGenNormalizedSdf3Paths(Collection<LanguageIdentifier> sourceDeps) {
        File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());

        final List<String> paths = new LinkedList<>();
        
        paths.add(srcGenSyntaxDir.getAbsolutePath());

        for(LanguageIdentifier langId : sourceDeps) {
            final @Nullable ILanguageComponent component = context.languageService().getComponent(langId);
            if(component == null) {
                report("Cannot get normalized SDF3 exports for language component with ID " + langId + ", it does not exist. Skipping");
                continue;
            }
            final ILanguageComponentConfig config = component.config();
            final Collection<IExportConfig> exports = config.exports();
            for(IExportConfig exportConfig : exports) {
                exportConfig.accept(new IExportVisitor() {
                    @Override public void visit(LangDirExport export) {
                        if(export.language.equals(SpoofaxConstants.LANG_ATERM_NAME)) {
                            try {
                                final FileObject dir = ResourceUtils.resolveFile(component.location(), export.directory);
                                paths.add(toFileReplicate(dir).getAbsolutePath());
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
        
        return paths;
    }
}
