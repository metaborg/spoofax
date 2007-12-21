package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.Term.applAt;
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

}
