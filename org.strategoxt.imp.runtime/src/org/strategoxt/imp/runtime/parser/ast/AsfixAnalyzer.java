package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.Term.applAt;
import static org.spoofax.jsglr.Term.asAppl;
import static org.spoofax.jsglr.Term.isAppl;
import static org.spoofax.jsglr.Term.termAt;
import static org.strategoxt.imp.runtime.Environment.getATermFactory;
import jjtraveler.Visitable;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;

public class AsfixAnalyzer {
	
	protected static final AFun AMB_FUN = getATermFactory().makeAFun("amb", 1, false);
	
	protected static final AFun OPT_FUN = getATermFactory().makeAFun("opt", 1, false);
	
	private static final AFun CF_FUN = getATermFactory().makeAFun("cf", 1, false);
	
	private static final AFun LEX_FUN = getATermFactory().makeAFun("lex", 1, false);
	
	private static final AFun LIT_FUN = getATermFactory().makeAFun("lit", 1, false);
	
	private static final AFun CILIT_FUN = getATermFactory().makeAFun("cilit", 1, false);
	
	private static final AFun VARSYM_FUN = getATermFactory().makeAFun("varsym", 1, false);
	
	private static final AFun LAYOUT_FUN = getATermFactory().makeAFun("layout", 0, false);
	
	private static final AFun SEQ_FUN = getATermFactory().makeAFun("seq", 2, false);
	
	private static final AFun ITER_FUN = getATermFactory().makeAFun("iter", 1, false);
	
	private static final AFun ITER_STAR_FUN = getATermFactory().makeAFun("iter-star", 1, false);
	
	private static final AFun ITER_PLUS_FUN = getATermFactory().makeAFun("iter-plus", 1, false);
	
	private static final AFun ITER_SEP_FUN = getATermFactory().makeAFun("iter-sep", 2, false);
	
	private static final AFun ITER_STAR_SEP_FUN = getATermFactory().makeAFun("iter-star-sep", 2, false);
	
	private static final AFun ITER_PLUS_SEP_FUN = getATermFactory().makeAFun("iter-plus-sep", 2, false);

	public static boolean isLayout(ATermAppl sort) {
		ATerm details = termAt(sort, 0);
		if (!isAppl(details))
			return false;
		
		if (OPT_FUN == asAppl(details).getAFun())
			details = applAt(details, 0);
		
		return LAYOUT_FUN == asAppl(details).getAFun();
	}

	public static boolean isLiteral(ATermAppl sort) {
		AFun fun = sort.getAFun();
		return LIT_FUN == fun || CILIT_FUN == fun;
	}

	public static boolean isList(ATermAppl sort) {
		ATermAppl details = CF_FUN == sort.getAFun()
		                  ? applAt(sort, 0)
		                  : sort;
		              	
	  	if (details.getAFun() == OPT_FUN)
	  		details = applAt(details, 0);
	  	
		AFun fun = details.getAFun();
		
		 // FIXME: Spoofax/159: AsfixImploder creates tuples instead of lists for seqs
		return isIterFun(fun) || SEQ_FUN == fun;
	}

	public static boolean isIterFun(AFun fun) {
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
		return (LEX_FUN == rhs.getAFun() || isLiteral(rhs)
		    || isLayout(rhs));
	}

	/**
	 * Identifies parse tree nodes that begin variables.
	 * 
	 * @see #isVariableNode(ATermAppl) 
	 * @return true if the current node is lexical.
	 */
	public static boolean isVariableNode(ATermAppl rhs) {
		return VARSYM_FUN == rhs.getAFun();
	}

	public static boolean isLexLayout(ATermAppl rhs) {
		if (rhs.getChildCount() != 1) return false;
		Visitable child = rhs.getChildAt(0);
		return child instanceof ATermAppl && LAYOUT_FUN == ((ATermAppl) child).getAFun()
			&& LEX_FUN == rhs.getAFun();
	}

}
