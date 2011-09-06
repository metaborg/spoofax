package org.strategoxt.imp.runtime.stratego;

import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Name of the language this AST belongs to.
 * @author Tobi Vollebregt
 */
public class OriginLanguagePrimitive extends AbstractPrimitive {

	public OriginLanguagePrimitive() {
		super("SSL_EXT_origin_language", 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		final IParseController pc = SourceAttachment.getParseController(tvars[0]);
		if (pc == null) return false;
		final ITermFactory factory = env.getFactory();
		env.setCurrent(factory.makeString(pc.getLanguage().getName()));
		return true;
	}

}
