package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.strategoxt.imp.runtime.parser.ast.StringAstNode;

public class WrappedAstNodeString extends WrappedAstNode implements IStrategoString {

	private final StringAstNode wrappee;
	
	protected WrappedAstNodeString(WrappedAstNodeFactory factory, StringAstNode node) {
		super(factory, node);
		this.wrappee = node;
	}

	public String getValue() {
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
    	pp.print("\"");
    	pp.print(wrappee.getValue());
    	pp.print("\"");
    }
}
