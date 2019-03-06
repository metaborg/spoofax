package org.metaborg.spoofax.core.tracing;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.tracing.Hover;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.tracing.TracingCommon.TermWithRegion;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

public class StrategoHoverFacet implements IHoverFacet {
    public final String strategyName;

    private @Inject IStrategoRuntimeService strategoRuntimeService;
    private @Inject TracingCommon common;

    public StrategoHoverFacet(String strategyName) {
        this.strategyName = strategyName;
    }


    @Override public Hover hover(FileObject source, IContext context, ILanguageComponent contributor,
        Iterable<IStrategoTerm> inRegion) throws MetaborgException {
        final HybridInterpreter interpreter;
        if(context == null) {
            interpreter = strategoRuntimeService.runtime(contributor, source, true);
        } else {
            interpreter = strategoRuntimeService.runtime(contributor, context, true);
        }
        final TermWithRegion tuple =
            common.outputs(interpreter, context.location(), source, inRegion, strategyName);
        return hover(tuple);
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
