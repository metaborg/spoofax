package org.metaborg.spoofax.core.tracing;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.tracing.Resolution;
import org.metaborg.core.tracing.ResolutionTarget;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.tracing.TracingCommon.TermWithRegion;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;

public class StrategoResolverFacet implements IResolverFacet {
    private static final ILogger logger = LoggerUtils.logger(StrategoResolverFacet.class);
    public final String strategyName;

    private @Inject IStrategoRuntimeService strategoRuntimeService;
    private @Inject TracingCommon common;


    public StrategoResolverFacet(String strategyName) {
        this.strategyName = strategyName;
    }


    @Override public Resolution resolve(FileObject source, IContext context, Iterable<IStrategoTerm> inRegion,
        ILanguageComponent contributor) throws MetaborgException {
        final HybridInterpreter interpreter;
        if(context == null) {
            interpreter = strategoRuntimeService.runtime(contributor, source, true);
        } else {
            interpreter = strategoRuntimeService.runtime(contributor, context, true);
        }
        final TermWithRegion tuple;
        try(IClosableLock lock = context.read()) {
            tuple = common.outputs(interpreter, source, source, inRegion, strategyName);
        }
        return resolve(tuple);
    }

    private Resolution resolve(@Nullable TermWithRegion tuple) {
        if(tuple == null) {
            return null;
        }

        final IStrategoTerm output = tuple.term;
        final ISourceRegion offsetRegion = tuple.region;

        final Collection<ResolutionTarget> targets = Lists.newLinkedList();
        if(output.getTermType() == IStrategoTerm.LIST) {
            for(IStrategoTerm subterm : output) {
                final String hyperlinkText = getHyperlinkText(subterm);
                final ISourceLocation targetLocation = common.getTargetLocation(subterm);
                if(targetLocation == null) {
                    logger.debug("Cannot get target location for {}", subterm);
                    continue;
                }
                targets.add(new ResolutionTarget(hyperlinkText, targetLocation));
            }
        } else {
            final String hyperlinkText = getHyperlinkText(output);
            final ISourceLocation targetLocation = common.getTargetLocation(output);
            if(targetLocation == null) {
                logger.debug("Reference resolution failed, cannot get target location for {}", output);
                return null;
            }
            targets.add(new ResolutionTarget(hyperlinkText, targetLocation));
        }

        if(targets.isEmpty()) {
            logger.debug("Reference resolution failed, cannot get target locations for {}", output);
            return null;
        }
        return new Resolution(offsetRegion, targets);
    }


    private @Nullable String getHyperlinkText(IStrategoTerm subterm) {
        for (IStrategoTerm annoterm : subterm.getAnnotations()) {
            if(Tools.isTermAppl(annoterm)
                    && Tools.hasConstructor((IStrategoAppl) annoterm, "HyperlinkText", 1)) {
                IStrategoTerm hyperlinkTextTerm = Tools.termAt(annoterm, 0);
                if(Tools.isTermString(hyperlinkTextTerm))
                    return Tools.asJavaString(hyperlinkTextTerm);
                else
                    return hyperlinkTextTerm.toString();
            }
        }
        return null;
    }
}
