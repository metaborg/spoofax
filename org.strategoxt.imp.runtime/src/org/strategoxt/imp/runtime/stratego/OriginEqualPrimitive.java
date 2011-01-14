package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

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
		boolean leftHasNode = t1 instanceof OneOfThoseTermsWithOriginInformation;
		boolean rightHasNode = t2 instanceof OneOfThoseTermsWithOriginInformation;
		if (leftHasNode && rightHasNode) {
			return ((IStrategoTerm) t1).getNode() == ((IStrategoTerm) t2).getNode();
		} else /* if (t1.getSubtermCount() > 0 && t2.getsu (leftHasNode && t1.getSubtermCount() > 0) {
			return equalChildTokens((IStrategoTerm) t1, t2);
		} else if (rightHasNode && t2.getSubtermCount() > 0) {
			return equalChildTokens((IStrategoTerm) t2, t1);
		} else */ {
			return false;
		}
	}

	/*
	private static boolean equalChildTokens(IStrategoTerm t1, IStrategoTerm t2) {
		IStrategoTerm child = termAt(t1, 0);
		if (child instanceof OneOfThoseTermsWithOriginInformation) {
			IToken start = t1.getNode().getLeftToken();
			IToken startChild = ((IStrategoTerm) termAt(t1, 0)).getNode().getLeftToken();
			if (start == startChild)
				return equal(termAt(t1, 0), t2);
		}
		return false;
	}
	*/

}
