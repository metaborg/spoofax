package org.metaborg.spoofax.core.outline;

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
import org.metaborg.core.outline.IOutline;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class OutlineService implements ISpoofaxOutlineService {
    private static final ILogger logger = LoggerUtils.logger(OutlineService.class);

    private final IProjectService projectService;
    private final IContextService contextService;
    private final IStrategoCommon common;


    @Inject public OutlineService(IProjectService projectService, IContextService contextService,
        IStrategoCommon common) {
        this.projectService = projectService;
        this.contextService = contextService;
        this.common = common;
    }


    @Override public boolean available(ILanguageImpl language) {
        return language.facetContribution(IOutlineFacet.class) != null;
    }

    @Override public IOutline outline(ISpoofaxParseUnit result) throws MetaborgException {
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

        final FacetContribution<IOutlineFacet> facetContrib = facet(langImpl);
        final IOutlineFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        try {
            return outline(source, context, facet, contributor, result.ast(), source);
        } catch(MetaborgException e) {
            throw new MetaborgException("Creating outline failed", e);
        }
    }


    @Override public IOutline outline(ISpoofaxAnalyzeUnit result) throws MetaborgException {
        if(!result.valid() || !result.hasAst()) {
            return null;
        }

        final FileObject source = result.source();
        final IContext context = result.context();
        final ILanguageImpl language = context.language();

        final FacetContribution<IOutlineFacet> facetContrib = facet(language);
        final IOutlineFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;

        try {
            return outline(source, context, facet, contributor, result.ast(), context.location());
        } catch(MetaborgException e) {
            throw new MetaborgException("Creating outline failed", e);
        }
    }


    private IOutline outline(FileObject source, IContext context, IOutlineFacet facet, ILanguageComponent contributor,
        IStrategoTerm ast, FileObject location) throws MetaborgException {
        final IBuilderInput input = common.builderInputTerm(ast, source, location);
        return facet.createOutline(source, context, contributor, input);
    }


    private FacetContribution<IOutlineFacet> facet(ILanguageImpl language) throws MetaborgException {
        final FacetContribution<IOutlineFacet> facet = language.facetContribution(IOutlineFacet.class);
        if(facet == null) {
            final String message =
                logger.format("Cannot create outline for {}, it does not have an outline facet", language);
            throw new MetaborgException(message);
        }
        return facet;
    }
}
