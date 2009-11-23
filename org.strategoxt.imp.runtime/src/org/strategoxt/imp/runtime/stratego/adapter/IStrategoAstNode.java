package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.ArrayList;

import lpg.runtime.IAst;

import org.eclipse.core.resources.IResource;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Interface for an AST node that can be converted into an ATerm. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IStrategoAstNode extends IAst {
	
	// TODO: Make independent from IAst interface??
	
	IStrategoTerm getTerm();
	
	String getConstructor();
	
	void setConstructor(String constructor);
	
	String getSort();
	
	IResource getResource();
	
	String yield();
	
	// SPECIALIZED FROM PARENT INTERFACE
	
	IStrategoAstNode getNextAst();
    
	IStrategoAstNode getParent();
	
	// getChildren() is also published here to avoid dependencies on IAst
	// (concrete, unparameterized type exposed by IAst interface)
	ArrayList getChildren();
}
