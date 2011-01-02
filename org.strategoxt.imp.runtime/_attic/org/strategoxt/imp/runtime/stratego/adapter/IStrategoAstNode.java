package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.ArrayList;

import lpg.runtime.ISimpleTerm;

import org.eclipse.core.resources.IResource;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Interface for an AST node that can be converted into an IStrategoTerm. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface ISimpleTerm extends ISimpleTerm {
	
	// TODO: Make independent from ISimpleTerm interface??
	
	IStrategoTerm;
	
	int getTermType();
	
	IStrategoList getAnnotations();
	
	String getConstructor();
	
	void setConstructor(String constructor);
	
	String getSort();
	
	IResource getResource();
	
	SGLRParseController getParseController();
	
	/**
	 * Retrieves the input string for this AST node.
	 */
	String yield();
	
	ISimpleTerm getRoot();
	
	boolean isList();
	
	// SPECIALIZED FROM PARENT INTERFACE
	
	ISimpleTerm getNextAst();
    
	ISimpleTerm getParent();
	
	// getChildren() is also published here to avoid dependencies on ISimpleTerm
	// (concrete, unparameterized type exposed by ISimpleTerm interface)
	ArrayList getChildren();
}
