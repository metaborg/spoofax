package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IStrategoTerm extends IStrategoTerm {
	ISimpleTerm getNode();
}
