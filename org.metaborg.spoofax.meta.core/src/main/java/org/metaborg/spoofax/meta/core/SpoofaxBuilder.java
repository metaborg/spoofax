package org.metaborg.spoofax.meta.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileTypeSelector;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.build.BuildOrder;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.ITransformerGoal;
import org.metaborg.spoofax.core.transform.TransformerException;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;

public class SpoofaxBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxBuilder.class);

    private final IContextService contextService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;
    private final ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer;


    @Inject public SpoofaxBuilder(IContextService contextService, ILanguageIdentifierService languageIdentifierService,
        ISyntaxService<IStrategoTerm> syntaxService, IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService,
        ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer) {
        this.contextService = contextService;
        this.languageIdentifierService = languageIdentifierService;
        this.syntaxService = syntaxService;
        this.analysisService = analysisService;
        this.transformer = transformer;
    }


    public void build(final ITransformerGoal goal, final Iterable<FileObject> sourceLocations,
        final Iterable<FileObject> includeLocations, final Collection<ILanguage> pardonedLanguages) throws Exception {

        final Multimap<ILanguage, FileObject> sourcesPerLang = HashMultimap.create();
        for(FileObject sourceLocation : sourceLocations) {
            for(FileObject sourceFile : expand(sourceLocation)) {
                ILanguage language = languageIdentifierService.identify(sourceFile);
                if(language != null) {
                    sourcesPerLang.put(language, sourceFile);
                }
            }
        }

        final Multimap<ILanguage, FileObject> includesPerLang = HashMultimap.create();
        for(FileObject includeLocation : includeLocations) {
            for(FileObject includeFile : expand(includeLocation)) {
                ILanguage language = languageIdentifierService.identify(includeFile);
                if(language != null) {
                    includesPerLang.put(language, includeFile);
                }
            }
        }

        doBuild(goal, sourcesPerLang, includesPerLang, pardonedLanguages);
    }

    public void build(final ITransformerGoal goal, final Multimap<ILanguage, FileObject> sourceLocationsPerLang,
        final Multimap<ILanguage, FileObject> includeLocationsPerLang, final Collection<ILanguage> pardonedLanguages)
        throws Exception {

        Multimap<ILanguage, FileObject> sourcesPerLang = HashMultimap.create();
        for(Entry<ILanguage, FileObject> sourceLocations : sourceLocationsPerLang.entries()) {
            for(FileObject sourceFile : expand(sourceLocations.getValue())) {
                if(languageIdentifierService.identify(sourceFile, sourceLocations.getKey())) {
                    sourcesPerLang.put(sourceLocations.getKey(), sourceFile);
                }
            }
        }

        Multimap<ILanguage, FileObject> includesPerLang = HashMultimap.create();
        for(Entry<ILanguage, FileObject> includeLocations : includeLocationsPerLang.entries()) {
            for(FileObject includeFile : expand(includeLocations.getValue())) {
                if(languageIdentifierService.identify(includeFile, includeLocations.getKey())) {
                    includesPerLang.put(includeLocations.getKey(), includeFile);
                }
            }
        }

        doBuild(goal, sourcesPerLang, includesPerLang, pardonedLanguages);
    }

    private Iterable<FileObject> expand(FileObject fileOrDirectory) throws FileSystemException {
        if(fileOrDirectory.exists()) {
            return Iterables2.from(fileOrDirectory.findFiles(new FileTypeSelector(FileType.FILE)));
        }
        return Iterables2.empty();
    }

    private void doBuild(final ITransformerGoal goal, final Multimap<ILanguage, FileObject> sources,
        final Multimap<ILanguage, FileObject> includes, final Collection<ILanguage> pardonedLanguages) throws Exception {
        List<ILanguage> compileLanguages = BuildOrder.sort(sources.keySet());
        for(ILanguage language : compileLanguages) {
            final Collection<FileObject> includeFiles =
                includes.containsKey(language) ? includes.get(language) : Collections.<FileObject>emptyList();
            doBuild(language, goal, sources.get(language), includeFiles, pardonedLanguages.contains(language));
        }
    }

    private void doBuild(final ILanguage language, final ITransformerGoal goal, final Iterable<FileObject> sources,
        final Iterable<FileObject> includes, final boolean pardoned) throws Exception {

        // build files
        final Multimap<IContext, FileObject> analysisFilesPerContext = HashMultimap.create();
        final Multimap<IContext, FileObject> transformFilesPerContext = HashMultimap.create();
        for(FileObject source : sources) {
            IContext context = contextService.get(source, language);
            analysisFilesPerContext.put(context, source);
            transformFilesPerContext.put(context, source);
        }
        for(FileObject include : includes) {
            IContext context = contextService.get(include, language);
            analysisFilesPerContext.put(context, include);
        }

        // parse, analyse and transform
        for(IContext context : transformFilesPerContext.keySet()) {
            Collection<FileObject> transformFiles = transformFilesPerContext.get(context);
            Collection<FileObject> analysisFiles = analysisFilesPerContext.get(context);
            Collection<ParseResult<IStrategoTerm>> parseResults = parseFiles(analysisFiles, language);
            if(!parseResults.isEmpty()) {
                log.info("Processing {} files in {}", context.language().name(), context.location());
                AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
                    analyseFiles(context, parseResults, pardoned);
                transformFiles(context, goal, analysisResult, transformFiles);
            }
        }
    }

    private Collection<ParseResult<IStrategoTerm>> parseFiles(final Collection<FileObject> files,
        final ILanguage language) throws SpoofaxException {
        final Collection<ParseResult<IStrategoTerm>> parseResults = Lists.newArrayList();
        final Collection<IMessage> parseMessages = Lists.newArrayList();
        for(FileObject file : files) {
            log.debug(String.format("Parsing %s as %s", file.getName(), language.name()));
            try {
                String text = CharStreams.toString(new InputStreamReader(file.getContent().getInputStream()));
                ParseResult<IStrategoTerm> parseResult = syntaxService.parse(text, file, language, null);
                parseResults.add(parseResult);
                parseMessages.addAll(Lists.newArrayList(parseResult.messages));
            } catch(IOException | ParseException ex) {
                throw new SpoofaxException("Error during parsing.", ex);
            }
        }
        if(printMessages(parseMessages, false)) {
            throw new SpoofaxException("There were parse errors.");
        }
        return parseResults;
    }

    private AnalysisResult<IStrategoTerm, IStrategoTerm> analyseFiles(final IContext context,
        final Iterable<ParseResult<IStrategoTerm>> parseResults, final boolean pardoned) throws SpoofaxException {
        AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult;
        log.debug(String.format("Analysing %s - %s", context.location(), context.language().name()));
        for(ParseResult<IStrategoTerm> parseResult : parseResults) {
            log.debug(String.format(" * %s", parseResult.source));
        }
        final Collection<IMessage> analysisMessages = Lists.newArrayList();
        try {
            synchronized(context) {
                analysisResult = analysisService.analyze(parseResults, context);
            }
            for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : analysisResult.fileResults) {
                analysisMessages.addAll(Lists.newArrayList(fileResult.messages));
            }
        } catch(AnalysisException ex) {
            throw new SpoofaxException("Analysis failed", ex);
        }
        if(printMessages(analysisMessages, pardoned)) {
            throw new SpoofaxException("There were analysis errors.");
        }
        return analysisResult;
    }

    private void transformFiles(final IContext context, final ITransformerGoal goal,
        final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult, final Collection<FileObject> targetFiles)
        throws SpoofaxException {
        if(!transformer.available(goal, context)) {
            log.debug(String.format("Transformer %s not available for %s", goal, context.language().name()));
            return;
        }
        log.debug(String.format("Transforming %s - %s", context.location(), context.language().name()));
        synchronized(context) {
            for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : analysisResult.fileResults) {
                if(targetFiles.contains(fileResult.source)) {
                    log.debug(String.format(" * %s", fileResult.source));
                    try {
                        transformer.transform(fileResult, context, goal);
                    } catch(TransformerException ex) {
                        throw new SpoofaxException("Transformation failed", ex);
                    }
                }
            }
        }
    }

    // Message printing

    private boolean printMessages(Collection<IMessage> messages, boolean errorsAsWarnings) {
        boolean hasErrors = false;
        for(IMessage message : messages) {
            String messageText = message2string(message);
            switch(message.severity()) {
                case ERROR:
                    if(!errorsAsWarnings) {
                        hasErrors = true;
                    }
                    log.error(messageText);
                    break;
                case WARNING:
                    log.warn(messageText);
                    break;
                case NOTE:
                    log.info(messageText);
                    break;
            }
        }
        return hasErrors;
    }

    private String message2string(IMessage message) {
        return String.format("%s[%s]: %s", message.source().getName(), region2string(message.region()),
            message.message());
    }

    private String region2string(ISourceRegion region) {
        return String.format("%s,%s", region.startRow() + 1, region.startColumn() + 1);
    }
}
