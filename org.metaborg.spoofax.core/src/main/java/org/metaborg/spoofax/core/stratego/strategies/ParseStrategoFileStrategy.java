package org.metaborg.spoofax.core.stratego.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.strc.parse_stratego_file_0_0;


public class ParseStrategoFileStrategy extends parse_stratego_file_0_0 {
    private final ParseFileStrategy parseFileStrategy;


    @jakarta.inject.Inject @javax.inject.Inject public ParseStrategoFileStrategy(ParseFileStrategy parseFileStrategy) {
        this.parseFileStrategy = parseFileStrategy;
    }


    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return parseFileStrategy.invoke(context, current);
    }
}
