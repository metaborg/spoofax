package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.Arrays;

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

	public String stringValue() {
		return wrappee.getValue();
	}
	
	public IStrategoTerm[] getArguments() {
		return getAllSubterms();
	}

    public void prettyPrint(ITermPrinter pp) {
    	pp.print("\"");
    	pp.print(wrappee.getValue().replace("\"", "\\\""));
    	pp.print("\"");
    }

	public int getTermType() {
		return IStrategoTerm.STRING;
	}

	@Override
	public boolean slowCompare(IStrategoTerm second) {
		return second instanceof IStrategoString
			&& ((IStrategoString) second).stringValue().equals(stringValue());
	}
	
	@Override
	public int hashCode() {
		int result = 8865;
		result = result * 31 + stringValue().hashCode();
		result = result * 31 + Arrays.deepHashCode(getAllSubterms());
		return result;
	}
}
