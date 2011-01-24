package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getTokenizer;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ITokenizer;

/**
 * Returns the file text
 * @author Maartje de Jonge
 */
public class OriginSourceTextPrimitive extends AbstractOriginPrimitive {

	public OriginSourceTextPrimitive() {
		super("SSL_EXT_origin_sourcetext");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		ITokenizer lexStream = getTokenizer(origin);
		String sourcetext=lexStream.getInput();
		return env.getFactory().makeString(sourcetext);
	}
}
