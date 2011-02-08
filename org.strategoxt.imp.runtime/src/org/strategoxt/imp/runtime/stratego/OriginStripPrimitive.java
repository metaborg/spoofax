package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermConverter;
import org.spoofax.terms.attachments.OriginAttachment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginStripPrimitive extends AbstractPrimitive {

	public OriginStripPrimitive() {
		super("SSL_EXT_origin_strip", 0, 1);
	}
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		// We just convert it to a normal term and hope it won't
		// go original later.
		IStrategoTerm input = tvars[0];
		IStrategoTerm result;
		if (OriginAttachment.get(input) == null) {
			result = input;
		} else {
			result = TermConverter.convert(env.getFactory(), input);
			assert OriginAttachment.get(result) == null;
		}
		env.setCurrent(result);
		return true;
	}

}
