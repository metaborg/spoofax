package org.strategoxt.imp.runtime.stratego;

import java.util.ArrayList;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.*;

/**
 * Tokens, ignoring layout
 */
public class OriginNonLayoutTokensPrimitive extends AbstractOriginPrimitive {
	
	public OriginNonLayoutTokensPrimitive() {
		super("SSL_EXT_origin_non_layout_tokens");
	}

	@Override
	public IStrategoTerm call(IContext env, IStrategoTerm origin) {
		ITokenizer tokenizer=getTokenizer(origin);
		int startIndex=getLeftToken(origin).getIndex();
		int endIndex = getRightToken(origin).getIndex();
		ArrayList<IStrategoTerm> tokenStrings=new ArrayList<IStrategoTerm>();
		for (int i = startIndex; i <= endIndex; i++) {
			if(tokenizer.getTokenAt(i).getKind() != IToken.TK_LAYOUT)
				tokenStrings.add(env.getFactory().makeString(tokenizer.getTokenAt(i).toString()));
		}		
		return env.getFactory().makeList(tokenStrings);
	}
}
