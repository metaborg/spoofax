package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.Term.applAt;
import jjtraveler.Visitable;
import aterm.ATermAppl;

public class AsfixAnalyzer {

	public static boolean isLayout(ATermAppl sort) {
		ATermAppl details = applAt(sort, 0);
		
		if (details.getName().equals("opt"))
			details = applAt(details, 0);
		
		return details.getName().equals("layout");
	}

	public static boolean isLiteral(ATermAppl sort) {
		return sort.getName().equals("lit") || sort.getName().equals("cilit");
	}

	public static boolean isList(ATermAppl sort) {
		ATermAppl details = sort.getName().equals("cf")
		                  ? applAt(sort, 0)
		                  : sort;
		              	
	  	if (details.getName().equals("opt"))
	  		details = applAt(details, 0);
	  	
		String name = details.getName();
		
		return name.equals("iter") || name.equals("iter-star")  || name.equals("iter-plus")
				|| name.equals("iter-sep") || name.equals("seq") || name.equals("iter-star-sep")
				|| name.equals("iter-plus-sep");
	}

	/**
	 * Identifies lexical parse tree nodes.
	 * 
	 * @see AsfixAnalyzer#isVariableNode(ATermAppl)
	 *      Identifies variables, which are usually treated similarly to
	 *      lexical nodes.
	 * 
	 * @return true if the current node is lexical.
	 */
	public static boolean isLexicalNode(ATermAppl rhs) {
		return ("lex".equals(rhs.getName()) || isLiteral(rhs)
		    || isLayout(rhs));
	}

	/**
	 * Identifies parse tree nodes that begin variables.
	 * 
	 * @see #isVariableNode(ATermAppl) 
	 * @return true if the current node is lexical.
	 */
	public static boolean isVariableNode(ATermAppl rhs) {
		return "varsym".equals(rhs.getName());
	}

	public static boolean isLexLayout(ATermAppl rhs) {
		if (rhs.getChildCount() != 1) return false;
		Visitable child = rhs.getChildAt(0);
		return child instanceof ATermAppl && "layout".equals(((ATermAppl) child).getName())
			&& "lex".equals(rhs.getName());
	}

}
