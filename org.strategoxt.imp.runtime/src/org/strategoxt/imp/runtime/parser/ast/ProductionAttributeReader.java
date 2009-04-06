package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.Term.*;

import java.util.HashMap;
import java.util.Map;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;

/**
 * Extracts attributes from parse table productions.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class ProductionAttributeReader {
	private static final int PARAMETRIZED_SORT_NAME = 0;
	
	private static final int PARAMETRIZED_SORT_ARGS = 1;
	
	private static final int ALT_SORT_LEFT = 0;
	
	private static final int ALT_SORT_RIGHT = 1;

	private final Map<ATermAppl, String> sortCache = new HashMap<ATermAppl, String>();

	public String getConsAttribute(ATermAppl attrs) {
		ATerm consAttr = getAttribute(attrs, "cons");
		return consAttr == null ? null : ((ATermAppl) consAttr).getName();
	}
	
	public ATerm getAstAttribute(ATermAppl attrs) {
		return getAttribute(attrs, "ast");
	}

	/** Return the contents of a term attribute (e.g., "cons"), or null if not found. */
	private static ATerm getAttribute(ATermAppl attrs, String attrName) {
		if (attrs.getName().equals("no-attrs"))
			return null;
		
		ATermList list = termAt(attrs, 0);
		
		for (int i = 0; i < list.getLength(); i++) {
			ATerm attr = list.elementAt(i);
			
			if (attr instanceof ATermAppl) {
				ATermAppl namedAttr = (ATermAppl) attr;
				if (namedAttr.getName().equals("term")) {
					namedAttr = termAt(namedAttr, 0);
					
					if (namedAttr.getName().equals(attrName)) {
						return termAt(namedAttr, 0);
					}
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
    	while (node.getChildCount() > 0 && isAppl(node)) {
    		if (asAppl(node).getName().equals("sort"))
    			return applAt(node, 0).getName();
    		if (asAppl(node).getName().equals("alt"))
    			return getAltSortName(node);
    		if (asAppl(node).getName().equals("parameterized-sort"))
    			return getParameterizedSortName(node);
    		
    		node = termAt(node, 0);
    	}
    	
    	return null;
    }
    
    private static String getParameterizedSortName(ATerm node) {
    	StringBuilder result = new StringBuilder();
    	
    	result.append(applAt(node, PARAMETRIZED_SORT_NAME).getName());
    	result.append('_');
    	
		ATermList args = termAt(node, PARAMETRIZED_SORT_ARGS);
		
        for (ATermAppl arg = (ATermAppl) args.getFirst(); !args.getNext().isEmpty(); args = args.getNext()) {
			result.append(arg.getName());
		}
		
		return result.toString();
    }
    
    private String getAltSortName(ATerm node) {
		String left = getSort(applAt(node, ALT_SORT_LEFT));
		String right = getSort(applAt(node, ALT_SORT_RIGHT));
		
		// HACK: In the RTG, alt sorts appear with a number at the end
		return left + "_" + right + "0";
    }
}
