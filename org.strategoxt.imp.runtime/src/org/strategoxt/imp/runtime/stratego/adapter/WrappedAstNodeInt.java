package org.strategoxt.imp.runtime.stratego.adapter;

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

	public int getValue() {
		return wrappee.getValue();
	}
	
	public IStrategoTerm[] getArguments() {
		return getAllSubterms();
	}
	
	public boolean match(IStrategoTerm second) {
		// TODO Auto-generated method stub
		return false;
	}
    

    public void prettyPrint(ITermPrinter pp) {
    	pp.print("" + wrappee.getValue());
    }
}
