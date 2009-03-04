package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.ArrayList;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.ISourceInfo;

/**
 * Interface for an AST node that can be converted into an ATerm. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IStrategoAstNode extends IAst {
	
	// TODO: Make independent from IAst interface??
	
	IStrategoTerm getTerm();
	
	String getConstructor();
	
	String getSort();
	
	ISourceInfo getSourceInfo();
	
	// SPECIALIZED FROM PARENT INTERFACE
	
	IStrategoAstNode getNextAst();
    
	IStrategoAstNode getParent();
	
	// getChildren() is also published here to avoid dependencies on IAst
	// (concrete, unparameterized type exposed by IAst interface)
	ArrayList getChildren();
}
