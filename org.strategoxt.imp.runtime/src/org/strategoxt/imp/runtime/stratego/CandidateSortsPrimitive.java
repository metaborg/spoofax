package org.strategoxt.imp.runtime.stratego;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class CandidateSortsPrimitive extends AbstractPrimitive {
	
	private static Set<String> candidateSorts;

	public CandidateSortsPrimitive() {
		super("SSL_EXT_candidatesorts", 0, 0);
	}
	
	public static void setCandidateSorts(Set<String> sorts) {
		Environment.assertLock();
		candidateSorts = sorts;
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		Environment.assertLock();
		List<IStrategoTerm> sortTerms = new ArrayList<IStrategoTerm>();
		for (String sort : candidateSorts)
			sortTerms.add(env.getFactory().makeString(sort));
		env.setCurrent(env.getFactory().makeList(sortTerms));
		return true;
	}

}
