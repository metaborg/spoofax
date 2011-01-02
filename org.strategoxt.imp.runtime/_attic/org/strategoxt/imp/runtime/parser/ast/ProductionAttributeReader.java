package org.strategoxt.imp.runtime.parser.ast;

import static org.strategoxt.imp.runtime.Environment.getATermFactory;
import static org.spoofax.terms.Term.*;

import java.util.HashMap;
import java.util.Map;

import aterm.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import aterm.ATermAppl;
import org.spoofax.interpreter.terms.IStrategoList;

/**
 * Extracts attributes from parse table productions.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class ProductionAttributeReader {
	
	protected static final IStrategoConstructor SORT_FUN = getATermFactory().makeIStrategoConstructor("sort", 1, false);
	
	protected static final IStrategoConstructor PARAMETERIZED_SORT_FUN =
		getATermFactory().makeIStrategoConstructor("parameterized-sort", 2, false);
	
	protected static final IStrategoConstructor ATTRS_FUN = getATermFactory().makeIStrategoConstructor("attrs", 1, false);
	
	protected static final IStrategoConstructor NO_ATTRS_FUN = getATermFactory().makeIStrategoConstructor("no-attrs", 0, false);
	
	protected static final IStrategoConstructor PREFER_FUN = getATermFactory().makeIStrategoConstructor("prefer", 0, false);
	
	protected static final IStrategoConstructor AVOID_FUN = getATermFactory().makeIStrategoConstructor("avoid", 0, false);
	
	private static final IStrategoConstructor VARSYM_FUN = getATermFactory().makeIStrategoConstructor("varsym", 1, false);
	
	private static final IStrategoConstructor ALT_FUN = getATermFactory().makeIStrategoConstructor("alt", 2, false);
	
	private static final IStrategoConstructor CHAR_CLASS_FUN = getATermFactory().makeIStrategoConstructor("char-class", 1, false);
	
	private static final int PARAMETRIZED_SORT_NAME = 0;
	
	private static final int PARAMETRIZED_SORT_ARGS = 1;
	
	private static final int ALT_SORT_LEFT = 0;
	
	private static final int ALT_SORT_RIGHT = 1;

	private final Map<ATermAppl, String> sortCache = new HashMap<ATermAppl, String>();

	public String getConsAttribute(ATermAppl attrs) {
		IStrategoTerm consAttr = getAttribute(attrs, "cons");
		return consAttr == null ? null : ((ATermAppl) consAttr).getName();
	}
	
	// FIXME: support meta-var constructors
	public String getMetaVarConstructor(ATermAppl rhs) {
		if (rhs.getChildCount() == 1 && VARSYM_FUN == rhs.getIStrategoConstructor()) {
			return AsfixAnalyzer.isIterFun(((ATermAppl) rhs.getChildAt(0)).getIStrategoConstructor())
					? "meta-listvar"
					: "meta-var";
		}
		return null;
	}
	
	public IStrategoTerm getAstAttribute(ATermAppl attrs) {
		return getAttribute(attrs, "ast");
	}
	
	public boolean isIndentPaddingLexical(ATermAppl attrs) {
		return getAttribute(attrs, "indentpadding") != null;
	}

	/** Return the contents of a term attribute (e.g., "cons"), or null if not found. */
	public IStrategoTerm getAttribute(ATermAppl attrs, String attrName) {
		if (attrs.getIStrategoConstructor() == NO_ATTRS_FUN)
			return null;
		
		IStrategoList list = termAt(attrs, 0);
		
		for (int i = 0; i < list.getLength(); i++) {
			IStrategoTerm attr = list.elementAt(i);
			
			if (attr instanceof ATermAppl) {
				ATermAppl namedAttr = (ATermAppl) attr;
				if (namedAttr.getName().equals("term")) {
					namedAttr = termAt(namedAttr, 0);
					
					if (namedAttr.getName().equals(attrName))
						return namedAttr.getChildCount() == 1 ? termAt(namedAttr, 0) : namedAttr;
				}				
			}
		}
		
		return null; // no cons found
	}
	
	/** 
	 * Get the RTG sort name of a production RHS, or for lists, the RTG element sort name.
	 */
    public String getSort(ATermAppl rhs) {
    	// Cached since getParameterizedSortName() is rather expensive
    	String result = sortCache.get(rhs);
    	if (result != null) return result;
    	
    	result = getSortUncached(rhs);
    	sortCache.put(rhs, result);
    	return result;
    }
    
    private String getSortUncached(ATermAppl node) {
    	for (IStrategoTerm current = node; current.getChildCount() > 0 && isTermAppl(current); current = termAt(current, 0)) {
    		IStrategoConstructor cons = asAppl(current).getIStrategoConstructor();
			if (cons == SORT_FUN)
    			return applAt(current, 0).getName();
    		if (cons == PARAMETERIZED_SORT_FUN)
    			return getParameterizedSortName(current);
    		if (cons == CHAR_CLASS_FUN)
    			return null;
    		if (cons == ALT_FUN)
    			return getAltSortName(current);
    	}
    	
    	return null;
    }
    
    private static String getParameterizedSortName(IStrategoTerm node) {
    	StringBuilder result = new StringBuilder();
    	
    	result.append(applAt(node, PARAMETRIZED_SORT_NAME).getName());
    	result.append('_');
    	
		IStrategoList args = termAt(node, PARAMETRIZED_SORT_ARGS);
		
        for (ATermAppl arg = (ATermAppl) args.head(); !args.tail().isEmpty(); args = args.tail()) {
			result.append(arg.getName());
		}
		
		return result.toString();
    }
    
    private String getAltSortName(IStrategoTerm node) {
		String left = getSort(applAt(node, ALT_SORT_LEFT));
		String right = getSort(applAt(node, ALT_SORT_RIGHT));
		
		// HACK: In the RTG, alt sorts appear with a number at the end
		return left + "_" + right + "0";
    }
}
