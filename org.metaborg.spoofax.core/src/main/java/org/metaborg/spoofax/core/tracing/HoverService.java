package org.metaborg.spoofax.core.tracing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.tracing.Hover;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.api.IHoverText;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.tracing.TracingCommon.TermWithRegion;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

public class HoverService implements ISpoofaxHoverService {
    private static final ILogger logger = LoggerUtils.logger(HoverService.class);

    private final IProjectService projectService;
    private final IContextService contextService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;
    private final TracingCommon common;
    private final IDynamicClassLoadingService semanticProviderService;


    @Inject public HoverService(IProjectService projectService, IContextService contextService,
        IStrategoRuntimeService strategoRuntimeService, ISpoofaxTracingService tracingService,
        TracingCommon common, DynamicClassLoadingService semanticProviderService) {
        this.projectService = projectService;
        this.contextService = contextService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.common = common;
        this.semanticProviderService = semanticProviderService;
    }


    @Override public boolean available(ILanguageImpl language) {
        try {
            return facet(language) != null;
        } catch (MetaborgException e) {
            return false;
        }
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

        final FacetContribution<IFacet> facetContrib = facet(langImpl);
        final IFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final Iterable<IStrategoTerm> inRegion = tracingService.fragments(result, new SourceRegion(offset));

        try {
            return hover(source, context, contributor, facet, inRegion);
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

        final FacetContribution<IFacet> facetContrib = facet(language);
        final IFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final Iterable<IStrategoTerm> inRegion = tracingService.fragments(result, new SourceRegion(offset));

        try {
            return hover(source, context, contributor, facet, inRegion);
        } catch(MetaborgException e) {
            throw new MetaborgException("Getting hover tooltip information failed unexpectedly", e);
        }
    }


    private Hover hover(FileObject source, IContext context, ILanguageComponent contributor, IFacet facet,
            Iterable<IStrategoTerm> inRegion) throws MetaborgException {
        if(facet instanceof StrategoHoverFacet) {
            return strategoHover(source, context, contributor, ((StrategoHoverFacet) facet).strategyName, inRegion);
        }
        if(facet instanceof JavaHoverFacet) {
            return javaHover(context, contributor, ((JavaHoverFacet) facet).javaClassName, inRegion);
        }
        logger.warn("Outlining facet has unexpected type: ", facet.getClass());
        return null;
    }


    private Hover strategoHover(FileObject source, IContext context, ILanguageComponent contributor,
            String strategy, Iterable<IStrategoTerm> inRegion) throws MetaborgException {
        final HybridInterpreter interpreter;
        if(context == null) {
            interpreter = strategoRuntimeService.runtime(contributor, source, true);
        } else {
            interpreter = strategoRuntimeService.runtime(contributor, context, true);
        }
        final TermWithRegion tuple =
            common.outputs(interpreter, context.location(), source, inRegion, strategy);
        return hover(tuple);
    }


    private Hover javaHover(IContext env, ILanguageComponent contributor, String javaClassName,
            Iterable<IStrategoTerm> inRegion) throws MetaborgException {
        IHoverText hoverer = semanticProviderService.loadClass(contributor, javaClassName, IHoverText.class);
        String hoverText = null;
        ISourceLocation highlightLocation = null;
        for (IStrategoTerm region : inRegion) {
            hoverText = hoverer.createHoverText(env, region);
            if(hoverText != null) {
                highlightLocation = tracingService.location(region);
                break;
            }
        }
        if(hoverText == null) {
            return null;
        }
        return new Hover(highlightLocation.region(), hoverText);
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    private FacetContribution<IFacet> facet(ILanguageImpl language) throws MetaborgException {
        FacetContribution<IFacet> facet = (FacetContribution) language.facetContribution(StrategoHoverFacet.class);
        if(facet == null) {
            facet = (FacetContribution) language.facetContribution(JavaHoverFacet.class);
        }
        if(facet == null) {
            final String message =
                logger.format("Cannot get hover tooltip information for {}, it does not have a hover facet", language);
            throw new MetaborgException(message);
        }
        return facet;
    }

    private Hover hover(@Nullable TermWithRegion tuple) {
        if(tuple == null) {
            return null;
        }

        final IStrategoTerm output = tuple.term;
        final ISourceRegion offsetRegion = tuple.region;

        final String text;
        if(output.getTermType() == IStrategoTerm.STRING) {
            text = Tools.asJavaString(output);
        } else {
            text = output.toString();
        }
        final String massagedText = text.replace("\\\"", "\"").replace("\\n", "");

        return new Hover(offsetRegion, massagedText);
    }
}
