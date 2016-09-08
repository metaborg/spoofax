package org.metaborg.spoofax.core.outline;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.outline.IOutline;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.core.outline.Outline;
import org.metaborg.core.outline.OutlineNode;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class OutlineService implements ISpoofaxOutlineService {
    private static final ILogger logger = LoggerUtils.logger(OutlineService.class);

    private final IProjectService projectService;
    private final IContextService contextService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;
    private final IStrategoCommon common;


    @Inject public OutlineService(IProjectService projectService, IContextService contextService,
        IStrategoRuntimeService strategoRuntimeService, ISpoofaxTracingService tracingService, IStrategoCommon common) {
        this.projectService = projectService;
        this.contextService = contextService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.common = common;
    }


    @Override public boolean available(ILanguageImpl language) {
        return language.facet(OutlineFacet.class) != null;
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

        final FacetContribution<OutlineFacet> facetContrib = facet(langImpl);
        final OutlineFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final String strategy = facet.strategyName;

        try {
            final HybridInterpreter interpreter;
            if(context == null) {
                interpreter = strategoRuntimeService.runtime(contributor, source, true);
            } else {
                interpreter = strategoRuntimeService.runtime(contributor, context, true);
            }
            final IStrategoTerm input = common.builderInputTerm(result.ast(), source, source);
            final IStrategoTerm outlineTerm = common.invoke(interpreter, input, strategy);
            if(outlineTerm == null) {
                return null;
            }
            final IOutline outline = toOutline(outlineTerm, facet.expandTo, contributor.location());
            return outline;
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

        final FacetContribution<OutlineFacet> facetContrib = facet(language);
        final OutlineFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final String strategy = facet.strategyName;

        try {
            final HybridInterpreter interpreter = strategoRuntimeService.runtime(contributor, context, true);
            final IStrategoTerm input = common.builderInputTerm(result.ast(), source, context.location());
            final IStrategoTerm outlineTerm = common.invoke(interpreter, input, strategy);
            if(outlineTerm == null) {
                return null;
            }
            final IOutline outline = toOutline(outlineTerm, facet.expandTo, contributor.location());
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


    private @Nullable IOutline toOutline(IStrategoTerm term, int expandTo, FileObject location) {
        final Collection<IOutlineNode> roots = Lists.newLinkedList();
        if(term instanceof IStrategoList) {
            final IStrategoList termList = (IStrategoList) term;
            for(IStrategoTerm rootTerm : termList) {
                final IOutlineNode node = toOutlineNode(rootTerm, null, location);
                if(node != null) {
                    roots.add(node);
                }
            }
        } else {
            final IOutlineNode node = toOutlineNode(term, null, location);
            if(node != null) {
                roots.add(node);
            }
        }
        if(roots.isEmpty()) {
            return null;
        }
        return new Outline(roots, expandTo);
    }

    private @Nullable IOutlineNode toOutlineNode(IStrategoTerm term, @Nullable IOutlineNode parent,
        FileObject location) {
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
        final ISourceRegion region = region(labelTerm);

        final OutlineNode node = new OutlineNode(label, icon, region, parent);

        final IStrategoTerm nodesTerm = appl.getSubterm(1);
        for(IStrategoTerm nodeTerm : nodesTerm) {
            final IOutlineNode childNode = toOutlineNode(nodeTerm, node, location);
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

    private @Nullable ISourceRegion region(IStrategoTerm term) {
        final ISourceLocation location = tracingService.location(term);
        if(location != null) {
            return location.region();
        }
        return null;
    }
}
