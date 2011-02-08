package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.terms.Term.termAt;
import static org.spoofax.terms.Term.toInt;

import java.util.ArrayList;
import java.util.List;

import org.spoofax.jsglr.client.imploder.IToken;

import org.spoofax.NotImplementedException;
import org.strategoxt.imp.runtime.Environment;

import org.spoofax.interpreter.terms.IStrategoTerm;
import aterm.ATermAppl;
import aterm.ATermInt;
import org.spoofax.interpreter.terms.IStrategoList;
import aterm.ATermPlaceholder;


/**
 * Implodes {ast} annotations in asfix trees.
 * 
 * Note that this class assigns a null sort to all children
 * of the constructed IStrategoTerm.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AstAnnoImploder {

	private final AstNodeFactory factory;
	
	private final List<IStrategoTerm> placeholderValues;
	
	private final IToken leftToken, rightToken;
	
	public AstAnnoImploder(AstNodeFactory factory, List<IStrategoTerm> placeholderValues, IToken leftToken, IToken rightToken) {
		this.factory = factory;
		this.placeholderValues = placeholderValues;
		this.leftToken = leftToken;
		this.rightToken = rightToken;
	}
	
	public IStrategoTerm implode(IStrategoTerm ast, String sort) {
		// Placeholder terms are represented as strings; must parse them and fill in their arguments
		String astString = ast.toString();
		if (astString.startsWith("\"") && astString.endsWith("\"")) {
			astString = astString.substring(1, astString.length() - 1);
			astString = astString.replace("\\\\", "\\").replace("\\\"", "\"");
			ast = Environment.getATermFactory().parse(astString);
		}
		
		return toAstNode(ast, sort);
	}
	
	private IStrategoTerm toAstNode(IStrategoTerm term, String sort) {
		switch (term.getType()) {
			case IStrategoTerm.PLACEHOLDER:
				return placeholderToAstNode(term, sort);
				
			case IStrategoTerm.APPL:
				return applToAstNode(term, sort);
				
			case IStrategoTerm.LIST:
				return listToAstNode(term, sort);
				
			case IStrategoTerm.INT:
				ATermInt i = (ATermInt) term;
				return factory.createIntTerminal(sort, leftToken, i.getInt());
				
			case IStrategoTerm.REAL:
				throw new NotImplementedException("reals in {ast} attribute");
				
			default:
				throw new IllegalStateException("Unexpected term type encountered in {ast} attribute");
		}
	}
	
	private IStrategoTerm placeholderToAstNode(IStrategoTerm placeholder, String sort) {
		IStrategoTerm term = ((ATermPlaceholder) placeholder).getPlaceholder();
		if (term.getType() == IStrategoTerm.INT) {
			int id = toInt(term);
			if (1 <= id && id <= placeholderValues.size()) {
				return placeholderValues.get(id - 1);
			}
		} else if (term.getType() == IStrategoTerm.APPL) {
			String type = ((ATermAppl) term).getName();
			if ("conc".equals(type) && term.getChildCount() == 2) {
				IStrategoTerm left = toAstNode(termAt(term, 0), null);
				IStrategoTerm right = toAstNode(termAt(term, 1), null);
				if (isTermList(left) && isTermList(right)) {
					ArrayList<IStrategoTerm> children = left.getChildren();
					children.addAll(right.getChildren());
					return new ListAstNode(sort, leftToken, rightToken, children);
				}
			} else if ("yield".equals(type) && term.getChildCount() == 1) {
				throw new NotImplementedException("yield in {ast} attribute");
			}
		}
			
		throw new IllegalStateException("Error in syntax definition: illegal placeholder in {ast} attribute: " + placeholder);
	}
	
	private IStrategoTerm applToAstNode(IStrategoTerm term, String sort) {
		ATermAppl appl = (ATermAppl) term;
		ArrayList<IStrategoTerm> children = new ArrayList<IStrategoTerm>(appl.getChildCount());
		for (int i = 0; i < appl.getChildCount(); i++) {
			children.add(toAstNode(termAt(appl, i), null));
		}
		if (appl.isQuoted()) {
			return factory.createStringTerminal(sort, appl.getName(), leftToken);
		} else {
			return factory.createNonTerminal(sort, appl.getName(), leftToken, rightToken, children);
		}
	}
	
	private IStrategoTerm listToAstNode(IStrategoTerm term, String sort) {
		// TODO: Fishy (Spoofax/49)
		IStrategoList list = (IStrategoList) term;
		ArrayList<IStrategoTerm> children = new ArrayList<IStrategoTerm>(list.getChildCount());
		for (int i = 0; i < term.getChildCount(); i++) {
			children.add(toAstNode(termAt(term, i), null));
		}
		return factory.createList(sort, leftToken, rightToken, children);
	}
}
