package org.metaborg.spoofax.core.completion.jsglr;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class CompletionDefinition {
	public final IStrategoTerm sort;
	public final String description;
	public final Iterable<ICompletionItem> items;


	public CompletionDefinition(IStrategoTerm sort, String description, Iterable<ICompletionItem> items) {
		this.sort = sort;
		this.description = description;
		this.items = items;
	}
}