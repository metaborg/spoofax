package org.metaborg.spoofax.core.tracing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.tracing.Hover;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class HoverService implements ISpoofaxHoverService {
    private static final ILogger logger = LoggerUtils.logger(HoverService.class);

    private final IProjectService projectService;
    private final IContextService contextService;
    private final ISpoofaxTracingService tracingService;


    @Inject public HoverService(IProjectService projectService, IContextService contextService,
        ISpoofaxTracingService tracingService) {
        this.projectService = projectService;
        this.contextService = contextService;
        this.tracingService = tracingService;
    }


    @Override public boolean available(ILanguageImpl language) {
        return language.facetContribution(IHoverFacet.class) != null;
    }

    @Override public Hover hover(int offset, ISpoofaxParseUnit result) throws MetaborgException {
        if(!result.valid()) {
            return null;
        }

        final FileObject source = result.source();
        final IProject project = projectService.get(source);
        final ILanguageImpl langImpl = result.input().langImpl();
        @Nullable IContext context;
        if(project == null) {
            context = null;
        } else {
            try {
                context = contextService.get(source, project, langImpl);
            } catch(ContextException | MetaborgRuntimeException e) {
                // Failed to get a context, ignore and use the source file to get a stratego runtime later.
                context = null;
            }
        }

        final FacetContribution<IHoverFacet> facetContrib = facet(langImpl);
        final IHoverFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final Iterable<IStrategoTerm> inRegion = tracingService.fragments(result, new SourceRegion(offset));

        try {
            return facet.hover(source, context, contributor, inRegion);
        } catch(MetaborgException e) {
            throw new MetaborgException("Getting hover tooltip information failed unexpectedly", e);
        }
    }

    @Override public Hover hover(int offset, ISpoofaxAnalyzeUnit result) throws MetaborgException {
        if(!result.valid() || !result.hasAst()) {
            return null;
        }

        final FileObject source = result.source();
        final IContext context = result.context();
        final ILanguageImpl language = context.language();

        final FacetContribution<IHoverFacet> facetContrib = facet(language);
        final IHoverFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final Iterable<IStrategoTerm> inRegion = tracingService.fragments(result, new SourceRegion(offset));

        try {
            return facet.hover(source, context, contributor, inRegion);
        } catch(MetaborgException e) {
            throw new MetaborgException("Getting hover tooltip information failed unexpectedly", e);
        }
    }


    @SuppressWarnings({ "rawtypes", "unchecked" }) private FacetContribution<IHoverFacet> facet(ILanguageImpl language)
        throws MetaborgException {
        FacetContribution<IHoverFacet> facet = (FacetContribution) language.facetContribution(IHoverFacet.class);
        if(facet == null) {
            final String message =
                logger.format("Cannot get hover tooltip information for {}, it does not have a hover facet", language);
            throw new MetaborgException(message);
        }
        return facet;
    }
}
