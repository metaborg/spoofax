package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.Term.*;

import java.util.ArrayList;
import java.util.List;

import lpg.runtime.IToken;

import org.spoofax.NotImplementedException;
import org.strategoxt.imp.runtime.Environment;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermInt;
import aterm.ATermList;
import aterm.ATermPlaceholder;


/**
 * Implodes {ast} annotations in asfix trees.
 * 
 * Note that this class assigns a null sort to all children
 * of the constructed AstNode.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AstAnnoImploder {
	private final AstNodeFactory factory;
	
	private final List<AstNode> placeholderValues;
	
	private final IToken leftToken, rightToken; 
	
	public AstAnnoImploder(AstNodeFactory factory, List<AstNode> placeholderValues, IToken leftToken, IToken rightToken) {
		this.factory = factory;
		this.placeholderValues = placeholderValues;
		this.leftToken = leftToken;
		this.rightToken = rightToken;
	}
	
	public AstNode implode(ATerm ast, String sort) {
		// Placeholder terms are represented as strings; must parse them and fill in their arguments
		String astString = ast.toString();
		if (astString.startsWith("\"") && astString.endsWith("\"")) {
			astString = astString.substring(1, astString.length() - 1);
			ast = Environment.getWrappedATermFactory().getFactory().parse(astString);
		}
		
		return toAstNode(ast, sort);
	}
	
	private AstNode toAstNode(ATerm term, String sort) {
		switch (term.getType()) {
			case ATerm.PLACEHOLDER:
				return placeholderToAstNode(term, sort);
				
			case ATerm.APPL:
				return applToAstNode(term, sort);
				
			case ATerm.LIST:
				return listToAstNode(term, sort);
				
			case ATerm.INT:
				ATermInt i = (ATermInt) term;
				return factory.createTerminal(sort, i.getInt(), leftToken, rightToken);
				
			case ATerm.REAL:
				throw new NotImplementedException("reals in {ast} attribute");
				
			default:
				throw new IllegalStateException("Unexpected term type encountered in {ast} attribute");
		}
	}
	
	private AstNode placeholderToAstNode(ATerm placeholder, String sort) {
		ATerm term = ((ATermPlaceholder) placeholder).getPlaceholder();
		if (term.getType() == ATerm.INT) {
			int id = toInt(term);
			if (1 <= id && id <= placeholderValues.size()) {
				return placeholderValues.get(id - 1);
			}
		} else if (term.getType() == ATerm.APPL) {
			String type = ((ATermAppl) term).getName();
			if ("conc".equals(type) && term.getChildCount() == 2) {
				AstNode left = toAstNode(termAt(term, 0), null);
				AstNode right = toAstNode(termAt(term, 1), null);
				if (left instanceof ListAstNode && right instanceof ListAstNode) {
					ArrayList<AstNode> children = left.getChildren();
					children.addAll(right.getChildren());
					return new ListAstNode(sort, leftToken, rightToken, children);
				}
			} else if ("yield".equals(type) && term.getChildCount() == 1) {
				throw new NotImplementedException("yield in {ast} attribute");
			}
		}
			
		throw new IllegalStateException("Error in syntax definition: illegal placeholder " + placeholder);
	}
	
	private AstNode applToAstNode(ATerm term, String sort) {
		ATermAppl appl = (ATermAppl) term;
		ArrayList<AstNode> children = new ArrayList<AstNode>(appl.getChildCount());
		for (int i = 0; i < appl.getChildCount(); i++) {
			children.add(toAstNode(termAt(appl, i), null));
		}
		return factory.createNonTerminal(sort, appl.getName(), leftToken, rightToken, children);
	}
	
	private AstNode listToAstNode(ATerm term, String sort) {
		ATermList list = (ATermList) term;
		ArrayList<AstNode> children = new ArrayList<AstNode>(list.getChildCount());
		for (int i = 0; i < term.getChildCount(); i++) {
			children.add(toAstNode(termAt(term, i), null));
		}
		return factory.createList(sort, leftToken, rightToken, children);
	}
}
