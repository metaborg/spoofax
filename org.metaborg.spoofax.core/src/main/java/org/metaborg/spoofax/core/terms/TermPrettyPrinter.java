package org.metaborg.spoofax.core.terms;

import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.aterm_escape_strings_0_0;
import org.strategoxt.stratego_aterm.pp_aterm_box_0_0;
import org.strategoxt.stratego_gpp.box2text_string_0_1;

import com.google.inject.Inject;

public class TermPrettyPrinter {
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ITermFactoryService termFactoryService;


    @Inject public TermPrettyPrinter(IStrategoRuntimeService strategoRuntimeService,
        ITermFactoryService termFactoryService) {
        this.strategoRuntimeService = strategoRuntimeService;
        this.termFactoryService = termFactoryService;
    }


    /**
     * Pretty prints an ATerm.
     * 
     * @param term
     *            ATerm to pretty print.
     * @return Pretty printed ATerm as a Stratego string.
     */
    public IStrategoString prettyPrint(IStrategoTerm term) {
        final Context context = strategoRuntimeService.genericRuntime().getCompiledContext();
        final ITermFactory termFactory = termFactoryService.getGeneric();
        org.strategoxt.stratego_aterm.Main.init(context);
        term = aterm_escape_strings_0_0.instance.invoke(context, term);
        term = pp_aterm_box_0_0.instance.invoke(context, term);
        term = box2text_string_0_1.instance.invoke(context, term, termFactory.makeInt(120));
        return (IStrategoString) term;
    }
}
