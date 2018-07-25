package org.metaborg.spoofax.core.tracing;

import static org.spoofax.interpreter.core.Tools.termAt;

import javax.annotation.Nullable;

import org.metaborg.core.language.IFacet;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.Term;

public class ResolverFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(ResolverFacetFromESV.class);

    public static @Nullable IFacet createResolver(IStrategoAppl esv) {
        final IStrategoAppl resolver = ESVReader.findTerm(esv, "ReferenceRule");
        if(resolver == null) {
            return null;
        }
        IStrategoTerm callTerm = termAt(resolver, 1);
        final String name = ESVReader.termContents(callTerm);
        if(name == null) {
            logger.error("Could not get contents of ESV ReferenceRule {}, cannot create resolver facet", resolver);
            return null;
        }

        switch (Term.tryGetName(callTerm)) {
            case "Java":
                return new JavaResolverFacet(name);
            default:
                return new StrategoResolverFacet(name);
        }
    }

    public static @Nullable StrategoHoverFacet createHover(IStrategoAppl esv) {
        final IStrategoAppl hover = ESVReader.findTerm(esv, "HoverRule");
        if(hover == null) {
            return null;
        }
        final String strategyName = ESVReader.termContents(termAt(hover, 1));
        if(strategyName == null) {
            logger.error("Could not get contents of ESV HoverRule {}, cannot create hover facet", hover);
            return null;
        }
        return new StrategoHoverFacet(strategyName);
    }
}
