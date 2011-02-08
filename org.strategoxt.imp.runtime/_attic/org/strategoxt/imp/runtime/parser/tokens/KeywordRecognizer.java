package org.strategoxt.imp.runtime.parser.tokens;

import static java.util.Collections.synchronizedMap;
import static org.spoofax.terms.Term.termAt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.spoofax.jsglr.Label;
import org.spoofax.jsglr.client.ParseTable;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

import aterm.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import aterm.ATermAppl;

/**
 * Recognizes keywords in a language without considering their context.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class KeywordRecognizer {

	private static final Map<Descriptor, KeywordRecognizer> cache =
		synchronizedMap(new WeakHashMap<Descriptor, KeywordRecognizer>());
	
	private static final IStrategoConstructor litFun = Environment.getATermFactory().makeIStrategoConstructor("lit", 1, false);
	
	private final Set<String> keywords = new HashSet<String>();
	
	private KeywordRecognizer(ParseTable table) {
		if (table != null) {
			for (Label l : table.getLabels()) {
				if (l != null) {
					IStrategoTerm rhs = termAt(l.getProduction(), 1);
					if (rhs instanceof ATermAppl && ((ATermAppl) rhs).getIStrategoConstructor() == litFun) {
						ATermAppl lit = termAt(rhs, 0);
						String litString = lit.getName();
						if (TokenKindManager.isKeyword(litString))
							keywords.add(litString);
					}
				}
			}
		}
	}
	
	public static KeywordRecognizer create(Descriptor d) {
		KeywordRecognizer result = cache.get(d);
		if (result == null) {
			try {
				Debug.startTimer();
				ParseTable table = Environment.getParseTableProvider(d.getLanguage()).get();
				Debug.stopTimer("Keyword recognizer loaded for " + d.getLanguage().getName());
				result = new KeywordRecognizer(table);
				cache.put(d, result);
			} catch (Exception e) {
				Environment.logException("Unexpected exception initializing the keyword recognizer", e);
				return new KeywordRecognizer(null);
			}
		}
		return result;
	}
	
	public boolean isKeyword(String literal) {
		return keywords.contains(literal);
	}
}
