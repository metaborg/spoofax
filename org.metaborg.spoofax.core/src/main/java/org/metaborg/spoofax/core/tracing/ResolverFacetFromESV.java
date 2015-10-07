package org.metaborg.spoofax.core.tracing;

import static org.spoofax.interpreter.core.Tools.termAt;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class ResolverFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(ResolverFacetFromESV.class);

    public static @Nullable ResolverFacet createResolver(IStrategoAppl esv) {
        final IStrategoAppl resolver = ESVReader.findTerm(esv, "ReferenceRule");
        if(resolver == null) {
            return null;
        }
        final String strategyName = ESVReader.termContents(termAt(resolver, 1));
        if(strategyName == null) {
            logger.error("Could not get contents of ESV ReferenceRule {}, cannot create resolver facet", resolver);
            return null;
        }
        return new ResolverFacet(strategyName);
    }

    public static @Nullable HoverFacet createHover(IStrategoAppl esv) {
        final IStrategoAppl hover = ESVReader.findTerm(esv, "HoverRule");
        if(hover == null) {
            return null;
        }
        final String strategyName = ESVReader.termContents(termAt(hover, 1));
        if(strategyName == null) {
            logger.error("Could not get contents of ESV HoverRule {}, cannot create hover facet", hover);
            return null;
        }
        return new HoverFacet(strategyName);
    }
}
