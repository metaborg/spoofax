package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoTerm;

import lpg.runtime.IAst;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IWrappedAstNode extends IStrategoTerm {
	IAst getNode();
}
