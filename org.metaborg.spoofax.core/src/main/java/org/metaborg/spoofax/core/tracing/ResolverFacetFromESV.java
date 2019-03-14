package org.metaborg.spoofax.core.tracing;

import static org.spoofax.interpreter.core.Tools.termAt;

import javax.annotation.Nullable;

import org.metaborg.core.language.IFacet;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.language.IFacetFactory;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.Term;

public class ResolverFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(ResolverFacetFromESV.class);

    public static @Nullable IFacet createResolver(IFacetFactory facetFactory, IStrategoAppl esv) {
        final IStrategoAppl resolver = ESVReader.findTerm(esv, "ReferenceRule");
        if(resolver == null) {
            return null;
        }
        final IStrategoTerm callTerm = termAt(resolver, 1);
        final String name = ESVReader.termContents(callTerm);
        if(name == null) {
            logger.error("Could not get contents of ESV ReferenceRule {}, cannot create resolver facet", resolver);
            return null;
        }

        switch (Term.tryGetName(callTerm)) {
            case "JavaGenerated":
                return facetFactory.javaGeneratedResolverFacet();
            case "Java":
                return facetFactory.javaResolverFacet(name);
            default:
                return facetFactory.strategoResolverFacet(name);
        }
    }

    public static @Nullable IFacet createHover(IFacetFactory facetFactory, IStrategoAppl esv) {
        final IStrategoAppl hover = ESVReader.findTerm(esv, "HoverRule");
        if(hover == null) {
            return null;
        }
        final IStrategoTerm callTerm = termAt(hover, 1);
        final String name = ESVReader.termContents(callTerm);
        if(name == null) {
            logger.error("Could not get contents of ESV HoverRule {}, cannot create hover facet", hover);
            return null;
        }

        switch (Term.tryGetName(callTerm)) {
            case "JavaGenerated":
                return facetFactory.javaGeneratedHoverFacet();
            case "Java":
                return facetFactory.javaHoverFacet(name);
            default:
                return facetFactory.strategoHoverFacet(name);
        }
    }
}
