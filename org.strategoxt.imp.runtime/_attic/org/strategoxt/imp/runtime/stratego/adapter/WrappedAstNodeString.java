package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

public class WrappedAstNodeString extends WrappedAstNode implements IStrategoString {
	
	protected WrappedAstNodeString(ISimpleTerm node) {
		super(node);
		assert isTermString(node);
	}

	public String stringValue() {
		return ((StringAstNode) getNode()).getValue();
	}

    public void prettyPrint(ITermPrinter pp) {
    	pp.print("\"");
    	pp.print(stringValue().replace("\\", "\\\\").replace("\"", "\\\"")
    			.replace("\n", "\\n").replace("\r", "\\r"));
    	pp.print("\"");
    	printAnnotations(pp);
    }

	public int getTermType() {
		return IStrategoTerm.STRING;
	}

	@Override
	protected boolean doSlowMatch(IStrategoTerm second, int commonStorageType) {
		return second.getTermType() == IStrategoTerm.STRING
			&& ((IStrategoString) second).stringValue().equals(stringValue())
			&& second.getAnnotations().equals(getAnnotations());
	}
	
	@Override
	public int hashFunction() {
		return stringValue().hashCode();
	}
}
