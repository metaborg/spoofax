package org.metaborg.spoofax.core.outline;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.outline.IOutline;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.core.outline.Outline;
import org.metaborg.core.outline.OutlineNode;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput;
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

import com.google.common.collect.Lists;

public class StrategoOutlineFacet implements IOutlineFacet {
    private static final ILogger logger = LoggerUtils.logger(StrategoOutlineFacet.class);

    public final String strategyName;
    public final int expandTo;
    private @Inject ISpoofaxTracingService tracingService;
    private @Inject IStrategoRuntimeService strategoRuntimeService;
    private @Inject IStrategoCommon common;

    public StrategoOutlineFacet(String strategyName, int expandTo) {
        this.strategyName = strategyName;
        this.expandTo = expandTo;
    }


    @Override public int getExpansionLevel() {
        return expandTo;
    }

    @Override public IOutline createOutline(FileObject source, IContext context, ILanguageComponent contributor,
        IBuilderInput input) throws MetaborgException {
        final String strategy = this.strategyName;
        final HybridInterpreter interpreter;
        if(context == null) {
            interpreter = strategoRuntimeService.runtime(contributor, source, true);
        } else {
            interpreter = strategoRuntimeService.runtime(contributor, context, true);
        }
        final IStrategoTerm outlineTerm = common.invoke(interpreter, input, strategy);
        if(outlineTerm == null) {
            return null;
        }
        final IOutline outline = toOutline(outlineTerm, this.getExpansionLevel(), contributor.location());
        return outline;
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

    private static String label(IStrategoTerm term) {
        if(term instanceof IStrategoString) {
            final IStrategoString stringTerm = (IStrategoString) term;
            return stringTerm.stringValue();
        } else {
            return term.toString();
        }
    }

    private static @Nullable FileObject icon(IStrategoTerm term, FileObject location) {
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
