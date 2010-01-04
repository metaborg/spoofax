package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.lang.compat.sglr.STRSGLR_anno_location;

import aterm.ATerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPAnnoLocationPrimitive extends STRSGLR_anno_location {
	
	private final SourceMappings mappings;
	
	public IMPAnnoLocationPrimitive(SourceMappings mappings) {
		this.mappings = mappings;
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		IStrategoTerm oldAsfix = tvars[0];
		
		boolean result = super.call(env, svars, tvars);
		if (!result) return false;
		
		// Restore tree metadata mappings
		IStrategoTerm newAsfix = env.current();
		char[] oldChars = mappings.getInputChars(oldAsfix);
		if (oldChars == null) return true;

		ATerm newAsfixTerm = Environment.getATermConverter().convert(newAsfix);
		mappings.putInputChars(newAsfix, oldChars);
		mappings.putInputTerm(newAsfix, newAsfixTerm);
		mappings.putInputFile(newAsfix, mappings.getInputFile(oldAsfix));
		mappings.putTokenizer(newAsfix, mappings.getTokenizer(oldAsfix));
		
		return true;
	}
}
