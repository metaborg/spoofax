package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IStrategoAstNode extends IAst {
	IStrategoTerm getTerm(WrappedAstNodeFactory factory);
	
	String getConstructor();
}
