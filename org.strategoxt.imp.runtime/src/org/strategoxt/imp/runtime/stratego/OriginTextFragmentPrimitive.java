package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;
import lpg.runtime.ILexStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;
import org.strategoxt.lang.terms.StrategoInt;
import org.strategoxt.lang.terms.StrategoTuple;

/**
 * Returns the textfragment that corresponds to the given char position (offset, offset-end)
 * @author Maartje de Jonge
 */
public class OriginTextFragmentPrimitive extends AbstractPrimitive {

private static final String NAME = "SSL_EXT_origin_textfragment";

	public OriginTextFragmentPrimitive() {
		super(NAME, 0, 2);
	}
	
	@Override 
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (!isTermString(tvars[0]) || !(tvars[1] instanceof StrategoTuple))
			return false;
		StrategoTuple position=(StrategoTuple)tvars[1];
		String textfragment=null;
		if (position.getSubtermCount()==3) {
			textfragment = textFromCharPosition(position);
		}
		if (textfragment == null) return false;
		IStrategoString result = env.getFactory().makeString(textfragment);
		env.setCurrent(result);
		return true;
	}

	private String textFromCharPosition(StrategoTuple position) {
		if(checkTuple(position))
			return null;
		int pos_start=((StrategoInt)position.get(0)).intValue();
		int pos_end=((StrategoInt)position.get(1)).intValue()-1;//exclusive
		ILexStream lexStream = ((IWrappedAstNode)position.get(2)).getNode().getLeftIToken().getILexStream();
		//ILexStream lexStream= EditorState.getActiveEditor().getParseController().getCurrentAst().getLeftIToken().getILexStream();
		if(DocumentaryStructure.isUnvalidInterval(pos_start, pos_end, lexStream))
			return null;
		String textfragment=lexStream.toString(pos_start, pos_end);
		return textfragment;
	}

	private boolean checkTuple(StrategoTuple position) {
		return !(
				position.get(0) instanceof StrategoInt && 
				position.get(1) instanceof StrategoInt &&
				position.get(2) instanceof IWrappedAstNode);
	}
}
