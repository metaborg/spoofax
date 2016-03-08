package org.metaborg.spoofax.core.build;

import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.build.Builder;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.core.processing.parse.IParseResultUpdater;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.transform.ITransformService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Typedef class for {@link Builder} with {@link IStrategoTerm}.
 */
public class SpoofaxBuilder extends Builder<IStrategoTerm, IStrategoTerm, IStrategoTerm> implements ISpoofaxBuilder {
    @Inject public SpoofaxBuilder(IResourceService resourceService, ILanguageIdentifierService languageIdentifier,
                                  ILanguagePathService languagePathService, IContextService contextService, ISourceTextService sourceTextService,
                                  ISyntaxService<IStrategoTerm> syntaxService, IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService,
                                  ITransformService<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformService,
                                  IParseResultUpdater<IStrategoTerm> parseResultProcessor,
                                  IAnalysisResultUpdater<IStrategoTerm, IStrategoTerm> analysisResultProcessor) {
        super(resourceService, languageIdentifier, languagePathService, contextService, sourceTextService,
            syntaxService, analysisService, transformService, parseResultProcessor, analysisResultProcessor);
    }
}
