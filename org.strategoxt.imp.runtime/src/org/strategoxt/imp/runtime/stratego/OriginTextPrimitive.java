package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getTokenizer;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginTextPrimitive extends AbstractOriginPrimitive {

	public OriginTextPrimitive() {
		super("SSL_EXT_origin_text");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		String result = getTokenizer(origin).toString(getLeftToken(origin), getRightToken(origin));
		//result = AutoEditStrategy.setIndentation(result, "");
		return env.getFactory().makeString(result);
	}

}
