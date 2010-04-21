package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;
import lpg.runtime.ILexStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
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
	
	@Override //TODO support (l,c,end-l,end-c)[see TextChangePrimitive]
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (!isTermString(tvars[0]) || !(tvars[1] instanceof StrategoTuple))
			return false;
		StrategoTuple position=(StrategoTuple)tvars[1];
		String textfragment=null;
		if (position.getSubtermCount()==2) {
			textfragment = textFromCharPosition(position);
		}
		else if (position.getSubtermCount()==4) {
			textfragment = textFromLocation(position);
		}	
		if (textfragment == null) return false;
		IStrategoString result = env.getFactory().makeString(textfragment);
		env.setCurrent(result);
		return true;
	}

	private String textFromCharPosition(StrategoTuple position) {
		if(!(position.get(0) instanceof StrategoInt && position.get(1) instanceof StrategoInt))
			return null;
		int pos_start=((StrategoInt)position.get(0)).intValue();
		int pos_end=((StrategoInt)position.get(1)).intValue();
		ILexStream lexStream= EditorState.getActiveEditor().getParseController().getCurrentAst().getLeftIToken().getILexStream();
		if(isBadLocation(pos_start, pos_end, lexStream))
			return null;
		String textfragment=lexStream.toString(pos_start, pos_end);
		return textfragment;
	}
	
	private String textFromLocation(StrategoTuple position) {
		if(!(position.get(0) instanceof StrategoInt && position.get(1) instanceof StrategoInt && position.get(2) instanceof StrategoInt && position.get(3) instanceof StrategoInt))
			return null;
		int line_start=((StrategoInt)position.get(0)).intValue();
		int col_start=((StrategoInt)position.get(1)).intValue();
		int line_end=((StrategoInt)position.get(2)).intValue();
		int col_end=((StrategoInt)position.get(3)).intValue();
		ILexStream lexStream= EditorState.getActiveEditor().getParseController().getCurrentAst().getLeftIToken().getILexStream();
		int pos_start=lexStream.getLineOffset(line_start)+col_start; //FIXME: bad location
		int pos_end=lexStream.getLineOffset(line_end)+col_end;
		if(isBadLocation(pos_start, pos_end, lexStream))
			return null;
		String textfragment=lexStream.toString(pos_start, pos_end);
		return textfragment;
	}

	private boolean isBadLocation(int pos_start, int pos_end, ILexStream lexStream) {
		return pos_start < 0 || pos_start > pos_end || pos_end >= lexStream.getStreamLength();
	}
	
	

}
