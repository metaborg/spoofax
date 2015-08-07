package org.metaborg.spoofax.core.stratego.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.RegisteringStrategy;
import org.strategoxt.lang.StrategyCollector;
import org.strategoxt.lang.linking.OverridingStrategy;

import com.google.inject.Inject;

@OverridingStrategy
public class ParseStrategoFileStrategy extends RegisteringStrategy {
	
	
    private final ParseFileStrategy parseFileStrategy;

    @Override
    public void registerImplementators(StrategyCollector collector) {
    	collector.registerStrategyImplementator("parse_stratego_file_0_0", this);
    }

    @Inject public ParseStrategoFileStrategy(ParseFileStrategy parseFileStrategy) {
        this.parseFileStrategy = parseFileStrategy;
    }


    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return parseFileStrategy.invoke(context, current);
    }
}
