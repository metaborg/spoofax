package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.terms.Term.applAt;
import static org.spoofax.terms.Term.asAppl;
import static org.spoofax.terms.Term.isTermAppl;
import static org.spoofax.terms.Term.termAt;
import static org.strategoxt.imp.runtime.Environment.getATermFactory;
import jjtraveler.Visitable;
import aterm.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import aterm.ATermAppl;

public class AsfixAnalyzer {
	
	protected static final IStrategoConstructor AMB_FUN = getATermFactory().makeIStrategoConstructor("amb", 1, false);
	
	protected static final IStrategoConstructor OPT_FUN = getATermFactory().makeIStrategoConstructor("opt", 1, false);
	
	private static final IStrategoConstructor CF_FUN = getATermFactory().makeIStrategoConstructor("cf", 1, false);
	
	private static final IStrategoConstructor LEX_FUN = getATermFactory().makeIStrategoConstructor("lex", 1, false);
	
	private static final IStrategoConstructor LIT_FUN = getATermFactory().makeIStrategoConstructor("lit", 1, false);
	
	private static final IStrategoConstructor CILIT_FUN = getATermFactory().makeIStrategoConstructor("cilit", 1, false);
	
	private static final IStrategoConstructor VARSYM_FUN = getATermFactory().makeIStrategoConstructor("varsym", 1, false);
	
	private static final IStrategoConstructor LAYOUT_FUN = getATermFactory().makeIStrategoConstructor("layout", 0, false);
	
	private static final IStrategoConstructor SEQ_FUN = getATermFactory().makeIStrategoConstructor("seq", 2, false);
	
	private static final IStrategoConstructor ITER_FUN = getATermFactory().makeIStrategoConstructor("iter", 1, false);
	
	private static final IStrategoConstructor ITER_STAR_FUN = getATermFactory().makeIStrategoConstructor("iter-star", 1, false);
	
	private static final IStrategoConstructor ITER_PLUS_FUN = getATermFactory().makeIStrategoConstructor("iter-plus", 1, false);
	
	private static final IStrategoConstructor ITER_SEP_FUN = getATermFactory().makeIStrategoConstructor("iter-sep", 2, false);
	
	private static final IStrategoConstructor ITER_STAR_SEP_FUN = getATermFactory().makeIStrategoConstructor("iter-star-sep", 2, false);
	
	private static final IStrategoConstructor ITER_PLUS_SEP_FUN = getATermFactory().makeIStrategoConstructor("iter-plus-sep", 2, false);

	public static boolean isLayout(ATermAppl sort) {
		IStrategoTerm details = termAt(sort, 0);
		if (!isTermAppl(details))
			return false;
		
		if (OPT_FUN == asAppl(details).getIStrategoConstructor())
			details = applAt(details, 0);
		
		return LAYOUT_FUN == asAppl(details).getIStrategoConstructor();
	}

	public static boolean isLiteral(ATermAppl sort) {
		IStrategoConstructor fun = sort.getIStrategoConstructor();
		return LIT_FUN == fun || CILIT_FUN == fun;
	}

	public static boolean isList(ATermAppl sort) {
		ATermAppl details = CF_FUN == sort.getIStrategoConstructor()
		                  ? applAt(sort, 0)
		                  : sort;
		              	
	  	if (details.getIStrategoConstructor() == OPT_FUN)
	  		details = applAt(details, 0);
	  	
		IStrategoConstructor fun = details.getIStrategoConstructor();
		
		 // FIXME: Spoofax/159: AsfixImploder creates tuples instead of lists for seqs
		return isIterFun(fun) || SEQ_FUN == fun;
	}

	public static boolean isIterFun(IStrategoConstructor fun) {
		return ITER_FUN == fun || ITER_STAR_FUN == fun || ITER_PLUS_FUN == fun
				|| ITER_SEP_FUN == fun || ITER_STAR_SEP_FUN == fun || ITER_PLUS_SEP_FUN == fun;
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
		return (LEX_FUN == rhs.getIStrategoConstructor() || isLiteral(rhs)
		    || isLayout(rhs));
	}

	/**
	 * Identifies parse tree nodes that begin variables.
	 * 
	 * @see #isVariableNode(ATermAppl) 
	 * @return true if the current node is lexical.
	 */
	public static boolean isVariableNode(ATermAppl rhs) {
		return VARSYM_FUN == rhs.getIStrategoConstructor();
	}

	public static boolean isLexLayout(ATermAppl rhs) {
		if (rhs.getChildCount() != 1) return false;
		Visitable child = rhs.getChildAt(0);
		return child instanceof ATermAppl && LAYOUT_FUN == ((ATermAppl) child).getIStrategoConstructor()
			&& LEX_FUN == rhs.getIStrategoConstructor();
	}

}
