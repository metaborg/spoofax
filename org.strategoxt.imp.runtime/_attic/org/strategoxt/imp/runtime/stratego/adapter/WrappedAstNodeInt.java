package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

public class WrappedAstNodeInt extends WrappedAstNode implements IStrategoInt {
	
	protected WrappedAstNodeInt(ISimpleTerm node) {
		super(node);
		assert node instanceof IntAstNode;
	}

	public int intValue() {
		return ((IntAstNode) getNode()).getValue();
	}
	
	@Override
	protected boolean doSlowMatch(IStrategoTerm second, int commonStorageType) {
		return second.getTermType() == IStrategoTerm.INT
			&& ((IStrategoInt) second).intValue() == intValue()
			&& second.getAnnotations().equals(getAnnotations());
	}

    public void prettyPrint(ITermPrinter pp) {
    	pp.print(String.valueOf(intValue()));
    	printAnnotations(pp);
    }

	public int getTermType() {
		return IStrategoTerm.INT;
	}
	
	@Override
	public int hashFunction() {
		return 449 * intValue() ^ 7841;
	}
}
