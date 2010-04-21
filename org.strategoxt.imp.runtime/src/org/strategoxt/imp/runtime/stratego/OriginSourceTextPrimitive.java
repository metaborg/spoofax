package org.strategoxt.imp.runtime.stratego;

import lpg.runtime.ILexStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;

/**
 * Returns the textfragment that corresponds to the current file
 * @author Maartje de Jonge
 */
public class OriginSourceTextPrimitive extends AbstractPrimitive {

private static final String NAME = "SSL_EXT_origin_source_text";

	public OriginSourceTextPrimitive() {
		super(NAME, 0, 2);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		ILexStream lexStream = EditorState.getActiveEditor().getCurrentAst()
			.getLeftIToken().getILexStream();
		String sourcetext=lexStream.toString(0, lexStream.getStreamLength()-1);
		IStrategoString result = env.getFactory().makeString(sourcetext);
		env.setCurrent(result);
		return true;
	}
}
