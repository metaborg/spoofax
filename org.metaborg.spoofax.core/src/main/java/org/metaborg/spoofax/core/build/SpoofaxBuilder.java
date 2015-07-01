package org.metaborg.spoofax.core.build;

import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.dialect.IDialectProcessor;
import org.metaborg.spoofax.core.language.dialect.IDialectService;
import org.metaborg.spoofax.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.spoofax.core.processing.parse.IParseResultUpdater;
import org.metaborg.spoofax.core.source.ISourceTextService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Typedef class for {@link Builder} with {@link IStrategoTerm}.
 */
public class SpoofaxBuilder extends Builder<IStrategoTerm, IStrategoTerm, IStrategoTerm> implements ISpoofaxBuilder {
    @Inject public SpoofaxBuilder(ILanguageIdentifierService languageIdentifier, IDialectService dialectService,
        IDialectProcessor dialectProcessor, IContextService contextService, ISourceTextService sourceTextService,
        ISyntaxService<IStrategoTerm> syntaxService, IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer,
        ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer,
        IParseResultUpdater<IStrategoTerm> parseResultProcessor,
        IAnalysisResultUpdater<IStrategoTerm, IStrategoTerm> analysisResultProcessor) {
        super(languageIdentifier, dialectService, dialectProcessor, contextService, sourceTextService, syntaxService,
            analyzer, transformer, parseResultProcessor, analysisResultProcessor);
    }
}
