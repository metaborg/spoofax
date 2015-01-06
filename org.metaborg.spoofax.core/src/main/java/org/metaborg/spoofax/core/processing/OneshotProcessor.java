package org.metaborg.spoofax.core.processing;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.language.AllLanguagesFileSelector;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseResult;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class OneshotProcessor<ParseT, AnalysisT> {
    private static final Logger logger = LogManager.getLogger(OneshotProcessor.class);

    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ISyntaxService<ParseT> parseService;
    private final IAnalysisService<ParseT, AnalysisT> analysisService;

    @Inject public OneshotProcessor(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISyntaxService<ParseT> parseService,
        IAnalysisService<ParseT, AnalysisT> analysisService) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.parseService = parseService;
        this.analysisService = analysisService;
    }

    public void process(String resourcesLocation) throws IOException {
        final FileObject resourcesDirectory = resourceService.resolve(resourcesLocation);
        final FileObject[] resources =
            resourcesDirectory.findFiles(new AllLanguagesFileSelector(languageIdentifierService));
        final Multimap<ILanguage, FileObject> allResourcesPerLang = LinkedHashMultimap.create();
        for(FileObject resource : resources) {
            final ILanguage language = languageIdentifierService.identify(resource);
            if(language != null) {
                allResourcesPerLang.put(language, resource);
            }
        }
        final int numLangs = allResourcesPerLang.keySet().size();
        final int numResources = allResourcesPerLang.values().size();

        final Multimap<ILanguage, ParseResult<ParseT>> allParseResults =
            LinkedHashMultimap.create(numLangs, numResources / numLangs);
        for(Entry<ILanguage, FileObject> entry : allResourcesPerLang.entries()) {
            final ILanguage language = entry.getKey();
            final FileObject resource = entry.getValue();

            try {
                final ParseResult<ParseT> parseResult = parseService.parse(resource, language);
                allParseResults.put(language, parseResult);

                // TODO: emit parse messages
            } catch(IOException e) {
                // TODO: emit error message
            }
        }

        final Multimap<ILanguage, AnalysisResult<ParseT, AnalysisT>> allAnalysisResults =
            LinkedHashMultimap.create(numLangs, numResources / numLangs);
        for(Entry<ILanguage, Collection<ParseResult<ParseT>>> entry : allParseResults.asMap().entrySet()) {
            final ILanguage language = entry.getKey();
            final Iterable<ParseResult<ParseT>> parseResults = entry.getValue();
            try {
                final AnalysisResult<ParseT, AnalysisT> analysisResult =
                    analysisService.analyze(parseResults, language);
                allAnalysisResults.put(language, analysisResult);
                
                // TODO: emit analysis messages
            } catch(SpoofaxException e) {
                // TODO: emit error message
            }
        }

        // TODO: execute actions
        // TODO: emit action messages
    }
}
