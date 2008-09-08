package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.Arrays;

import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.strategoxt.imp.runtime.parser.ast.IntAstNode;

public class WrappedAstNodeInt extends WrappedAstNode implements IStrategoInt {

	private final IntAstNode wrappee;
	
	protected WrappedAstNodeInt(WrappedAstNodeFactory factory, IntAstNode node) {
		super(factory, node);
		this.wrappee = node;
	}

	public int intValue() {
		return wrappee.getValue();
	}
	
	public IStrategoTerm[] getArguments() {
		return getAllSubterms();
	}
	
	@Override
	public boolean slowCompare(IStrategoTerm second) {
		return second instanceof IStrategoInt
			&& ((IStrategoInt) second).intValue() == intValue();
	}

    public void prettyPrint(ITermPrinter pp) {
    	pp.print("" + wrappee.getValue());
    }

	public int getTermType() {
		return IStrategoTerm.INT;
	}
	
	@Override
	public int hashCode() {
		int result = 345239057;
		result = result * 31 + intValue();
		result = result * 31 + Arrays.deepHashCode(getAllSubterms());
		return result;
	}
}
