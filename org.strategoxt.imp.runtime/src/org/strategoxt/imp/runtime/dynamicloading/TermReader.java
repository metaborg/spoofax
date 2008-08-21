package org.strategoxt.imp.runtime.dynamicloading;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Term reading utility class.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TermReader {
	private TermReader() {}
	
	public static IStrategoAppl findTerm(IStrategoTerm term, String constructor) {
		if (term instanceof IStrategoAppl && cons((IStrategoAppl) term).equals(constructor))
			return (IStrategoAppl) term;
		
		for (int i = 0; i < term.getSubtermCount(); i++) {
			IStrategoAppl result = findTerm(termAt(term, i), constructor);
			if (result != null) return result;
		}
		
		return null;
	}
	
	public static ArrayList<IStrategoAppl> collectTerms(IStrategoAppl term, String... constructors) {
		ArrayList<IStrategoAppl> results = new ArrayList<IStrategoAppl>();
		for (String constructor : constructors) {
			collectTerms(term, constructor, results);
		}
		return results;
	}
	
	private static void collectTerms(IStrategoAppl term, String constructor, ArrayList<IStrategoAppl> results) {
		for (int i = 0; i < term.getSubtermCount(); i++) {
			if (termAt(term, i).getTermType() == IStrategoTerm.APPL) {
				IStrategoAppl subterm = termAt(term, i);
				if (cons(subterm).equals(constructor))	results.add(subterm);
				collectTerms(subterm, constructor, results);
			}
		}
	}
	
	public static String termContents(IStrategoTerm t) {
		if (t == null) return null;
		String result = t.getTermType() == IStrategoTerm.STRING
		        ? t.toString()
		        : termAt(t, 0).toString();
		if (result.startsWith("\"") && result.endsWith("\""))
			result = result.substring(1, result.length() - 2);
		return result;				
	}

	public static String concatTermStrings(IStrategoTerm list) {
		IStrategoTerm values = termAt(list, 0);
		StringBuilder results = new StringBuilder();
		
		if (values.getSubtermCount() > 0)
			results.append(termContents(termAt(values, 0)));
		
		for (int i = 1; i <  values.getSubtermCount(); i++) {
			results.append(',');
			results.append(termContents(termAt(values, i)));
		}
		return results.toString();
	}
	
	public static int intAt(IStrategoTerm t, int index) {
		return Integer.parseInt(termContents(t.getSubterm(index)));
	}
	
	public static String cons(IStrategoAppl t) {
		return t.getConstructor().getName();
	}

    @SuppressWarnings("unchecked") // casting is inherently unsafe, but doesn't warrant a warning here
    public static<T extends IStrategoTerm> T termAt(IStrategoTerm t, int index) {
        return (T) t.getSubterm(index);
    }
}
