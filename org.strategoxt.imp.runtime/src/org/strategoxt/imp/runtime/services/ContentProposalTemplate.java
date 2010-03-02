/**
 * 
 */
package org.strategoxt.imp.runtime.services;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposalTemplate {
	
	private final String prefix;
	
	private final IStrategoList completionParts;

	public ContentProposalTemplate(String prefix, IStrategoList completionParts) {
		this.prefix = prefix;
		this.completionParts = completionParts;
	}

	public String getPrefix() {
		return prefix;
	}

	public IStrategoList getCompletionParts() {
		return completionParts;
	}

	public String getDescription() {
		StringBuilder result = new StringBuilder();
		for (IStrategoTerm part : completionParts.getAllSubterms()) {
			result.append(termContents(part));
		}
		return AutoEditStrategy.formatInsertedText(result.toString(), "");
	}
	
	public String getName() {
		return getPrefix();
	}
}
