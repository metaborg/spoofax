package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.eclipse.core.runtime.IPath;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Interface for an AST node that can be converted into an ATerm. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IStrategoAstNode extends IAst {
	IStrategoTerm getTerm();
	
	String getConstructor();
	
	IPath getResourcePath();
	
	IPath getRootPath();
	
	// SPECIALIZED FROM PARENT INTERFACE
	
	IStrategoAstNode getNextAst();
    
	IStrategoAstNode getParent();
}
