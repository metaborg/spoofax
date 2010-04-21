package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;
import lpg.runtime.ILexStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.lang.terms.StrategoInt;
import org.strategoxt.lang.terms.StrategoTuple;

/**
 * Extracts all comment lines directly after the current node on the same line
 * @author Maartje de Jonge
 */
public class OriginPositionToLocationPrimitive extends AbstractPrimitive {
	
	private static final String NAME = "SSL_EXT_origin_pos_to_loc";
	

	public OriginPositionToLocationPrimitive() {
		super(NAME, 0, 1);
	}
	
	@Override 
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (!(tvars[0] instanceof StrategoInt))
			return false;
		int position=((StrategoInt)tvars[0]).intValue();
		ILexStream lexStream= EditorState.getActiveEditor().getParseController().getCurrentAst().getLeftIToken().getILexStream();
		if(isBadLocation(position, lexStream))
			return false;
		int col=lexStream.getColumnOfCharAt(position);
		int line=lexStream.getLineNumberOfCharAt(position);
		IStrategoTuple result = env.getFactory().makeTuple(
				env.getFactory().makeInt(line),
				env.getFactory().makeInt(col)
		);
		env.setCurrent(result);
		return true;
	}

	private boolean isBadLocation(int pos, ILexStream lexStream) {
		return pos < 0 || pos >= lexStream.getStreamLength();
	}

}
