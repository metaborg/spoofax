package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginEqualPrimitive extends AbstractPrimitive {

	public OriginEqualPrimitive() {
		super("SSL_EXT_origin_equal", 0, 2);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		IStrategoTerm tvar1 = tvars[0];
		IStrategoTerm tvar2 = tvars[1];
		return equal(tvar1, tvar2);
	}

	private static boolean equal(IStrategoTerm t1, IStrategoTerm t2) {
		boolean leftHasNode = t1 instanceof IWrappedAstNode;
		boolean rightHasNode = t2 instanceof IWrappedAstNode;
		if (leftHasNode && rightHasNode) {
			return ((IWrappedAstNode) t1).getNode() == ((IWrappedAstNode) t2).getNode();
		} else /* if (t1.getSubtermCount() > 0 && t2.getsu (leftHasNode && t1.getSubtermCount() > 0) {
			return equalChildTokens((IWrappedAstNode) t1, t2);
		} else if (rightHasNode && t2.getSubtermCount() > 0) {
			return equalChildTokens((IWrappedAstNode) t2, t1);
		} else */ {
			return false;
		}
	}

	/*
	private static boolean equalChildTokens(IWrappedAstNode t1, IStrategoTerm t2) {
		IStrategoTerm child = termAt(t1, 0);
		if (child instanceof IWrappedAstNode) {
			IToken start = t1.getNode().getLeftIToken();
			IToken startChild = ((IWrappedAstNode) termAt(t1, 0)).getNode().getLeftIToken();
			if (start == startChild)
				return equal(termAt(t1, 0), t2);
		}
		return false;
	}
	*/

}
