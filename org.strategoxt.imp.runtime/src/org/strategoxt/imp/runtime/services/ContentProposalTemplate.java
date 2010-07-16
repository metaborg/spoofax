/**
 * 
 */
package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposalTemplate {
	
	private final String prefix;
	
	private final String sort;
	
	private final IStrategoList completionParts;
	
	private final boolean blankLineRequired;

	public ContentProposalTemplate(String prefix, String sort, IStrategoList completionParts, boolean blankLineRequired) {
		this.prefix = prefix;
		this.sort = sort;
		this.completionParts = completionParts;
		this.blankLineRequired = blankLineRequired;
	}
	
	public String getSort() {
		return sort;
	}

	public String getPrefix() {
		return prefix;
	}

	public IStrategoList getCompletionParts() {
		return completionParts;
	}
	
	public boolean isBlankLineRequired() {
		return blankLineRequired;
	}

	public String getDescription() {
		StringBuilder result = new StringBuilder();
		for (IStrategoTerm part : completionParts.getAllSubterms()) {
			if ("Placeholder".equals(cons(part))) {
				IStrategoString placeholder = termAt(part, 0);
				String contents = placeholder.stringValue();
				contents = contents.substring(1, contents.length() - 1); // strip < >
				result.append(contents);
			} else {
				result.append(termContents(part));
			}
		}
		return AutoEditStrategy.formatInsertedText(result.toString(), "");
	}
	
	public String getName() {
		return getPrefix();
	}
}
