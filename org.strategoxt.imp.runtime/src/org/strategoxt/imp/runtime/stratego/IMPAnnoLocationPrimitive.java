package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.adapter.aterm.WrappedATerm;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermConverter;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.lang.compat.sglr.STRSGLR_anno_location;

import aterm.ATerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPAnnoLocationPrimitive extends STRSGLR_anno_location {
	
	private final TermConverter converter = new TermConverter(Environment.getWrappedATermFactory());

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		IStrategoTerm oldAsfix = tvars[0];
		
		boolean result = super.call(env, svars, tvars);
		if (!result) return false;
		
		// Restore tree metadata mappings
		IStrategoTerm newAsfix = env.current();
		IOperatorRegistry registry = env.getOperatorRegistry(IMPJSGLRLibrary.REGISTRY_NAME);
		IMPParseStringPTPrimitive mapping = (IMPParseStringPTPrimitive) registry.get(IMPParseStringPTPrimitive.NAME);

		char[] oldChars = mapping.getInputChars(oldAsfix);
		if (oldChars == null) return true;

		// TODO: Optimize - Aagh stop it with all the term conversions!!
		ATerm newAsfixTerm = ((WrappedATerm) converter.convert(newAsfix)).getATerm();
		mapping.putInputChars(newAsfix, oldChars);
		mapping.putInputTerm(newAsfix, newAsfixTerm);
		
		JSGLRI.putTokenizer(newAsfixTerm, JSGLRI.getTokenizer(mapping.getInputTerm(oldAsfix)));
		
		return true;
	}
}
