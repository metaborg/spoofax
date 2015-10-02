package org.metaborg.spoofax.core.outline;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.outline.IOutline;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.core.outline.Outline;
import org.metaborg.core.outline.OutlineNode;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

public class OutlineService implements ISpoofaxOutlineService {
    private static final ILogger logger = LoggerUtils.logger(OutlineService.class);

    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;
    private final IStrategoCommon common;


    @Inject public OutlineService(IStrategoRuntimeService strategoRuntimeService,
        ISpoofaxTracingService tracingService, IStrategoCommon common) {
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.common = common;
    }


    @Override public boolean available(ILanguageImpl language) {
        return language.facet(OutlineFacet.class) != null;
    }

    @Override public IOutline outline(ParseResult<IStrategoTerm> result) throws MetaborgException {
        if(result.result == null) {
            return null;
        }

        final FileObject resource = result.source;
        final ILanguageImpl language = result.language;

        final FacetContribution<OutlineFacet> facetContrib = facet(language);
        final OutlineFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final String strategy = facet.strategyName;

        try {
            final HybridInterpreter interpreter = strategoRuntimeService.runtime(contributor, resource);
            final IStrategoTerm input = common.builderInputTerm(result.result, resource, resource);
            final IStrategoTerm outlineTerm = common.invoke(interpreter, input, strategy);
            if(outlineTerm == null) {
                return null;
            }
            final IOutline outline = toOutline(outlineTerm, facet.expandTo, contributor.location(), true);
            return outline;
        } catch(MetaborgException e) {
            throw new MetaborgException("Creating outline failed", e);
        }
    }

    @Override public IOutline outline(AnalysisFileResult<IStrategoTerm, IStrategoTerm> result) throws MetaborgException {
        if(result.result == null) {
            return null;
        }

        final IContext context = result.context;
        final ILanguageImpl language = context.language();

        final FacetContribution<OutlineFacet> facetContrib = facet(language);
        final OutlineFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final String strategy = facet.strategyName;

        try {
            final HybridInterpreter interpreter = strategoRuntimeService.runtime(contributor, context);
            final IStrategoTerm input = common.builderInputTerm(result.result, result.source, context.location());
            final IStrategoTerm outlineTerm = common.invoke(interpreter, input, strategy);
            if(outlineTerm == null) {
                return null;
            }
            final IOutline outline = toOutline(outlineTerm, facet.expandTo, contributor.location(), false);
            return outline;
        } catch(MetaborgException e) {
            throw new MetaborgException("Creating outline failed", e);
        }
    }


    private FacetContribution<OutlineFacet> facet(ILanguageImpl language) throws MetaborgException {
        final FacetContribution<OutlineFacet> facet = language.facetContribution(OutlineFacet.class);
        if(facet == null) {
            final String message =
                logger.format("Cannot create outline for {}, it does not have an outline facet", language);
            throw new MetaborgException(message);
        }
        return facet;
    }


    private @Nullable IOutline toOutline(IStrategoTerm term, int expandTo, FileObject location, boolean parsed) {
        final IOutlineNode node = toOutlineNode(term, null, location, parsed);
        if(node == null) {
            return null;
        }
        return new Outline(node, expandTo);
    }

    private @Nullable IOutlineNode toOutlineNode(IStrategoTerm term, @Nullable IOutlineNode parent,
        FileObject location, boolean parsed) {
        if(!(term instanceof IStrategoAppl)) {
            return null;
        }
        final IStrategoAppl appl = (IStrategoAppl) term;
        if(!Tools.hasConstructor(appl, "Node", 2)) {
            return null;
        }

        final IStrategoTerm labelTerm = appl.getSubterm(0);
        final String label = label(labelTerm);
        final FileObject icon = icon(labelTerm, location);
        final ISourceRegion region = region(labelTerm, parsed);

        final OutlineNode node = new OutlineNode(label, icon, region, parent);

        final IStrategoTerm nodesTerm = appl.getSubterm(1);
        for(IStrategoTerm nodeTerm : nodesTerm) {
            final IOutlineNode childNode = toOutlineNode(nodeTerm, node, location, parsed);
            if(childNode != null) {
                node.addChild(childNode);
            }
        }

        return node;
    }

    private String label(IStrategoTerm term) {
        if(term instanceof IStrategoString) {
            final IStrategoString stringTerm = (IStrategoString) term;
            return stringTerm.stringValue();
        } else {
            return term.toString();
        }
    }

    private @Nullable FileObject icon(IStrategoTerm term, FileObject location) {
        final IStrategoList annos = term.getAnnotations();
        if(annos == null) {
            return null;
        }
        if(annos.getSubtermCount() != 1) {
            return null;
        }
        final IStrategoTerm iconTerm = annos.getSubterm(0);
        if(!(iconTerm instanceof IStrategoString)) {
            return null;
        }
        final IStrategoString iconTermString = (IStrategoString) iconTerm;
        final String iconLocation = iconTermString.stringValue();
        try {
            return location.resolveFile(iconLocation);
        } catch(FileSystemException e) {
            logger.error("Cannot resolve icon {} in {}", e, iconLocation, location);
            return null;
        }
    }

    private @Nullable ISourceRegion region(IStrategoTerm term, boolean parsed) {
        final ISourceLocation location;
        if(parsed) {
            location = tracingService.fromParsed(term);
        } else {
            location = tracingService.fromAnalyzed(term);
        }
        if(location != null) {
            return location.region();
        }
        return null;
    }
}
