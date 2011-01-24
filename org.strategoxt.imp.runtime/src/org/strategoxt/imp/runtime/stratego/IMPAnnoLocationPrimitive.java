package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermAppl;

import java.io.File;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.SourceMappings.MappableTerm;
import org.strategoxt.lang.compat.sglr.STRSGLR_anno_location;

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
		MappableTerm newAsfix = new MappableTerm(env.current());
		env.setCurrent(newAsfix);
		
		String oldChars = mappings.getInputString(oldAsfix);
		if (oldChars == null)
			return true;

		File inputFile = isTermAppl(oldAsfix)
			? mappings.getInputFile((IStrategoAppl) oldAsfix)
			: null;
		mappings.putInputString(newAsfix, oldChars);
		mappings.putInputTerm(newAsfix, newAsfix);
		if (isTermAppl(newAsfix))
			mappings.putInputFileForTree(newAsfix, inputFile);
		
		return true;
	}
}
