package org.metaborg.spoofax.core.completion;

import static org.spoofax.interpreter.core.Tools.termAt;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class SemanticCompletionFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(SemanticCompletionFacetFromESV.class);

    public static @Nullable SemanticCompletionFacet create(IStrategoAppl esv) {
        final IStrategoAppl completer = ESVReader.findTerm(esv, "CompletionProposer");
        if(completer == null) {
            return null;
        }
        final String strategyName = ESVReader.termContents(termAt(completer, 1));
        if(strategyName == null) {
            logger.error(
                "Could not get contents of ESV CompletionProposer {}, cannot create semantic completions facet",
                completer);
            return null;
        }
        return new SemanticCompletionFacet(strategyName);
    }
}
