package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.ArrayList;

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
	
	String getSort();
	
	IPath getResourcePath();
	
	IPath getRootPath();
	
	// SPECIALIZED FROM PARENT INTERFACE
	
	IStrategoAstNode getNextAst();
    
	IStrategoAstNode getParent();
	
	// getChildren() is also published here to avoid dependencies on IAst
	// (concrete, unparameterized type exposed by IAst interface)
	ArrayList getChildren();
}
